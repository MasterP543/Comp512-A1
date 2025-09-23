package Server.RMI;

import Server.Common.Middleware;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware extends Middleware {
    private static String s_serverName = "Middleware";
    private static String s_rmiPrefix = "group_17_";

    private static String flights_ServerName = "Flights_Server";
    private static String cars_ServerName = "Cars_Server";
    private static String rooms_ServerName = "Rooms_Server";

    public static void main(String[] args) {

        if (args.length == 3) {
            flights_ServerName = args[0];
            cars_ServerName = args[1];
            rooms_ServerName = args[2];
        }

        // Create the RMI server entries for flights, cars and rooms
        try {


            Registry backendRegistry = LocateRegistry.getRegistry(3017);



            // Look up the stubs of the existing servers
            IResourceManager flightsStub = (IResourceManager) backendRegistry.lookup(s_rmiPrefix + flights_ServerName);
            IResourceManager carsStub    = (IResourceManager) backendRegistry.lookup(s_rmiPrefix + cars_ServerName);
            IResourceManager roomsStub   = (IResourceManager) backendRegistry.lookup(s_rmiPrefix + rooms_ServerName);

            RMIMiddleware middleware = new RMIMiddleware(s_serverName, flightsStub, carsStub, roomsStub);

            IResourceManager middlewareStub = (IResourceManager) UnicastRemoteObject.exportObject(middleware, 0);

            Registry middlewareRegistry;
            middlewareRegistry = LocateRegistry.getRegistry(3017);


            final Registry registry = middlewareRegistry;

            registry.rebind(s_rmiPrefix + s_serverName, middlewareStub);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + s_serverName);
                        System.out.println("'" + s_serverName + "' middleware unbound");
                    } catch (Exception e) {
                        System.err.println("Error during middleware shutdown");
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");

        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }


    }

    public RMIMiddleware(String s_serverName, IResourceManager flights, IResourceManager cars, IResourceManager rooms) throws RemoteException {
        super(s_serverName, flights, cars, rooms);
    }

}


