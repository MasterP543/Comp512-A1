package Server.TCP;

import Server.Common.ResourceManager;

import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;

public class TCPMiddlewareThread extends Thread{

    Socket socket;
    private static final int socketPort = 3017;
    private String flights_ServerHost;
    private String cars_ServerHost;
    private String rooms_ServerHost;

    private static ResourceManager customers = new ResourceManager("Customers");

    public TCPMiddlewareThread (String flights_ServerHost, String cars_ServerHost, String rooms_ServerHost, Socket socket) {
        this.flights_ServerHost = flights_ServerHost;
        this.cars_ServerHost = cars_ServerHost;
        this.rooms_ServerHost = rooms_ServerHost;
        this.socket=socket;
    }





    public void run() {


        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {

            System.out.println("Connected to client... " + socket.getInetAddress().toString());

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }




            while (true) {

                try {
                    Request req = (Request) ois.readObject();
                    Response res = new Response();

                    System.out.println("Received request for " + req.method);

                    String method = req.method;

                    if (method.contains("Flight")) {
                        res = sendToServer(flights_ServerHost, socketPort, req);
                    } else if (method.contains("Car")) {
                        res = sendToServer(cars_ServerHost, socketPort, req);
                    } else if (method.contains("Room")) {
                        res = sendToServer(rooms_ServerHost, socketPort, req);
                    } else if (method.contains("Customer")) {
                        //res = handleCustomer(req);
                        continue;
                    }
                    System.out.println("Sending response to client...");
                    oos.writeObject(res);
                    oos.flush();
                } catch (EOFException eof) {
                    System.out.println("Client disconnected: " + socket.getInetAddress());
                    break;  // exit loop, close thread
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.exit(1);
                }

            }

    }


    private Response sendToServer(String host, int port, Request req) throws IOException {
        Socket socket = new Socket(host, port);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject(req);   // send request
        oos.flush();

        try {
            Response response = (Response) ois.readObject(); // get response
            oos.close();
            ois.close();
            socket.close();
            return response;
        } catch (ClassNotFoundException e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            return null;
        }
    }
    private Response handleCustomer(Request request) throws RemoteException {
        String method = request.method;
        Response response = new Response();

        switch (method) {
            case "AddCustomer": {
                if (request.args.size() < 1) response.result = customers.newCustomer();
                else response.result = customers.newCustomer(Integer.parseInt((String) request.args.get(0)));
            }
            case "DeleteCustomer": {
                response.result = customers.deleteCustomer(Integer.parseInt((String) request.args.get(0)));
            }
            case "QueryCustomer": {
                response.result = customers.queryCustomerInfo(Integer.parseInt((String) request.args.get(0)));
            }
        }

        return response;
    }


}
