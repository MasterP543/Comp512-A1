package Server.TCP;

import Server.Common.ResourceManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPMiddleware {
    private String flights_ServerHost;
    private String cars_ServerHost;
    private String rooms_ServerHost;
    private static final int socketPort = 3017;

    private static ResourceManager customers = new ResourceManager("Customers");

    public TCPMiddleware(String flights_ServerHost, String cars_ServerHost, String rooms_ServerHost) {
        this.flights_ServerHost = flights_ServerHost;
        this.cars_ServerHost = cars_ServerHost;
        this.rooms_ServerHost = rooms_ServerHost;
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            String flights_ServerHost = args[0];
            String cars_ServerHost = args[1];
            String rooms_ServerHost = args[2];

            try {
                TCPMiddleware middleware = new TCPMiddleware(flights_ServerHost, cars_ServerHost, rooms_ServerHost);
                middleware.runServer();
            } catch (Exception e) {
                System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void runServer() throws IOException {

        ServerSocket serverSocket = new ServerSocket(socketPort);
        System.out.println("Middleware server ready...");

        Socket clientSocket = serverSocket.accept();
        System.out.println("Connected to client... " + clientSocket.getInetAddress().toString());

        while (true) {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            try {
                Request req = (Request) ois.readObject();
                Response res;

                System.out.println("Received request for " + req.method);

                String method = req.method;

                if (method.contains("Flight")) {
                    res = sendToServer(flights_ServerHost, socketPort, req);
                } else if (method.contains("Car")) {
                    res = sendToServer(cars_ServerHost, socketPort, req);
                } else if (method.contains("Room")) {
                    res = sendToServer(rooms_ServerHost, socketPort, req);
                } else break;
                System.out.println("Sending response to client...");
                oos.writeObject(res);
                oos.flush();
            }
            catch (IOException | ClassNotFoundException e) {
                System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
                System.exit(1);
            }
            oos.close();
            ois.close();
        }
        clientSocket.close();
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
}