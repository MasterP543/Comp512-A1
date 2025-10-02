package Server.TCP;

import Server.Common.ResourceManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPResourceManagerMultithread extends Thread{
    Socket socket;

    private static ResourceManager resourceManager;

    public TCPResourceManagerMultithread(Socket socket, ResourceManager resourceManager) {
        this.socket = socket;
        this.resourceManager = resourceManager;
    }

    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
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

                System.out.println("Handling request for " + req.method);
                switch (req.method) {
                    case "AddFlight":
                        res.result = resourceManager.addFlight(
                                (int) req.args.get(0),
                                (int) req.args.get(1),
                                (int) req.args.get(2)
                        );
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
                    case "RemoveReservationFlight":
                        res.result = resourceManager.removeReservationFlight(
                                (String) req.args.getFirst(),
                                (int) req.args.get(1)
                        );
                        break;
                    case "RemoveReservationCar":
                        res.result = resourceManager.removeReservationCar(
                                (String) req.args.getFirst(),
                                (int) req.args.get(1)
                        );
                        break;
                    case "RemoveReservationRoom":
                        res.result = resourceManager.removeReservationRoom(
                                (String) req.args.getFirst(),
                                (int) req.args.get(1)
                        );
                        break;
                }
                System.out.println("Success, sending response...");
                oos.writeObject(res);
                oos.flush();
            }catch (EOFException eof) {
                break;  // exit loop, close thread
            } catch(IOException | ClassNotFoundException e){
                System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
                e.printStackTrace();
                System.exit(1);
            }
        }
        //ois.close();
        //oos.close();
    }

}
