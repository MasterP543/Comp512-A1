package Server.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPMiddleware {
    private String flights_ServerHost;
    private String cars_ServerHost;
    private String rooms_ServerHost;
    private static final int socketPort = 3017;

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
                middleware.runServerMultiThread();
            } catch (Exception e) {
                System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void runServerMultiThread() throws IOException {
        ServerSocket serverSocket = new ServerSocket(socketPort);
        System.out.println("Middleware started on port " + socketPort);
        while (true)
        {
            Socket socket=serverSocket.accept();
            new TCPMiddlewareThread(flights_ServerHost, cars_ServerHost, rooms_ServerHost, socket).start();
        }
    }
}
