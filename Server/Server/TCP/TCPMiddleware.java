package Server.TCP;

import Server.Common.Middleware;
import Server.Interface.IResourceManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

public class TCPMiddleware {
    private String flights_ServerHost;
    private String cars_ServerHost;
    private String rooms_ServerHost;
    private static final int socketPort = 3017;


    private Response sendToServer(String host, int port, Request req) throws IOException {
        Socket socket = new Socket(host, port);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject(req);   // send request
        oos.flush();

        try {
            return (Response) ois.readObject();  // read reply
        } catch (ClassNotFoundException e) {

        }
        return null;
    }


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

            TCPMiddleware middleware = new TCPMiddleware(flights_ServerHost, cars_ServerHost, rooms_ServerHost);

            try {
                middleware.runServer();
            } catch (IOException e) {
            }
        }


    }

    public void runServer() throws IOException {

        ServerSocket serverSocket = new ServerSocket(socketPort);

        System.out.println("Middleware server ready...");

        while (true) {
            Socket clientSocket = serverSocket.accept();

            try {
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

                Request req = (Request) ois.readObject();
                Response res = new Response();

                String method = req.method;

                if (method.contains("Flight")) {
                    res = sendToServer(flights_ServerHost, socketPort, req);
                } else if (method.contains("Car")) {
                    res = sendToServer(cars_ServerHost, socketPort, req);
                } else if (method.contains("Room")) {
                    res = sendToServer(rooms_ServerHost, socketPort, req);
                }

                oos.writeObject(res);
                oos.flush();


            }
            catch (IOException | ClassNotFoundException ignored) {}
        }



    }


}
