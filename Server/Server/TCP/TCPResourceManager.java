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
            handleClient(clientSocket);
        }

    }

    public static void handleClient(Socket client) throws  IOException{
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        try {
            Request req = (Request) ois.readObject();
            Response res = new Response();

            System.out.println("Handling request for " + req.method);
            switch (req.method){
                case "AddFlight":
                    res.result = resourceManager.addFlight(
                            Integer.parseInt((String) req.args.get(0)),
                            Integer.parseInt((String) req.args.get(1)),
                            Integer.parseInt((String) req.args.get(2))
                    );
                    break;

                case "AddCars":
                    res.result = resourceManager.addCars(
                            (String) req.args.get(0),
                            Integer.parseInt((String) req.args.get(1)),
                            Integer.parseInt((String) req.args.get(2))
                    );
                    break;
                case "AddRooms":
                    res.result = resourceManager.addRooms(
                            (String) req.args.get(0),
                            Integer.parseInt((String) req.args.get(1)),
                            Integer.parseInt((String) req.args.get(2))
                    );
                    break;

                case "DeleteFlight":
                    res.result = resourceManager.deleteFlight(
                            Integer.parseInt((String) req.args.getFirst())
                    );
                    break;

                case "DeleteCars":
                    res.result = resourceManager.deleteCars(
                            (String) req.args.getFirst()
                    );
                    break;

                case "DeleteRooms":
                    res.result = resourceManager.deleteRooms(
                            (String) req.args.getFirst()
                    );
                    break;
                case "QueryFlight":
                    res.result = resourceManager.queryFlight(
                            Integer.parseInt((String) req.args.getFirst())
                    );
                    break;
                case "QueryRooms":
                    res.result = resourceManager.queryRooms(
                            (String) req.args.getFirst()
                    );
                    break;
                case "QueryCars":
                    res.result = resourceManager.queryCars(
                            (String) req.args.getFirst()
                    );
                    break;
                case "QueryFlightPrice":
                    res.result = resourceManager.queryFlightPrice(
                            Integer.parseInt((String) req.args.getFirst())
                    );
                    break;
                case "QueryCarsPrice":
                    res.result = resourceManager.queryCarsPrice(
                            (String) req.args.getFirst()
                    );
                    break;
                case "QueryRoomsPrice":
                    res.result = resourceManager.queryRoomsPrice(
                            (String) req.args.getFirst()
                    );
                    break;
                case "ReserveFlight":
                    res.result = resourceManager.reserveFlight(
                            Integer.parseInt((String) req.args.get(0)),
                            Integer.parseInt((String) req.args.get(1))
                    );
                    break;
                case "ReserveCar":
                    res.result = resourceManager.reserveCar(
                            Integer.parseInt((String) req.args.get(0)),
                            (String) req.args.get(1)
                    );
                    break;
                case "ReserveRoom":
                    res.result = resourceManager.reserveRoom(
                            Integer.parseInt((String) req.args.get(0)),
                            (String) req.args.get(1)
                    );
                    break;
                }
            System.out.println("Success, sending response...");
            oos.writeObject(res);
            oos.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
        ois.close();
        oos.close();
    }
}
