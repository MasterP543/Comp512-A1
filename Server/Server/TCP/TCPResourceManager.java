package Server.TCP;

import Server.Common.ResourceManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPResourceManager {

    private static int socketPort;
    private static ResourceManager resourceManager = new ResourceManager("ResourceManager");

    public static void main(String[] args) {
        socketPort = Integer.parseInt(args[0]);
        TCPResourceManager server = new TCPResourceManager();

        try {
            server.runServer();
        }
        catch (IOException ignored)
        {}
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




                switch (req.method){
                    case "AddFlight":
                        System.out.println("Adding flight...");
                        res.result = resourceManager.addFlight(
                                Integer.parseInt((String) req.args.get(0)),
                                Integer.parseInt((String) req.args.get(1)),
                                Integer.parseInt((String) req.args.get(2))
                        );
                        System.out.println("Result: " + res.result);
                        break;

                    case "AddCars":
                        res.result = resourceManager.addCars(
                                (String) req.args.get(0),
                                (int) req.args.get(1),
                                (int) req.args.get(2)
                        );
                        break;
                    case "AddRooms":
                        res.result = resourceManager.addRooms(
                                (String) req.args.get(0),
                                (int) req.args.get(1),
                                (int) req.args.get(2)
                        );
                        break;

                    case "DeleteFlight":
                        res.result = resourceManager.deleteFlight(
                                (int) req.args.getFirst()
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
                                (int) req.args.getFirst()
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
                                (int) req.args.getFirst()
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
                                (int) req.args.get(0),
                                (int) req.args.get(1)
                        );
                        break;
                    case "ReserveCar":
                        res.result = resourceManager.reserveCar(
                                (int) req.args.get(0),
                                (String) req.args.get(1)
                        );
                        break;
                    case "ReserveRoom":
                        res.result = resourceManager.reserveRoom(
                                (int) req.args.get(0),
                                (String) req.args.get(1)
                        );
                        break;

                }


            System.out.println("Sending response to Middleware...");
            oos.writeObject(res);
            oos.flush();

        } catch (IOException | ClassNotFoundException ignored) {

        }
        ois.close();
        oos.close();
    }
}
