package Server.TCP;

import Server.Common.ResourceManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPResourceManager {

    private static int socketPort = 3017;
    private static ResourceManager resourceManager;

    private TCPResourceManager(String serverName) {
        resourceManager = new ResourceManager(serverName);
    }
    public static void main(String[] args) {
        TCPResourceManager server = new TCPResourceManager(args[0]);

        try {
            server.runServer();
        }
        catch (IOException e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void runServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(socketPort);
        System.out.println("Server ready...");

        while (true){
            Socket clientSocket = serverSocket.accept();
            new TCPResourceManagerMultithread(clientSocket, resourceManager).start();
        }

    }

}
