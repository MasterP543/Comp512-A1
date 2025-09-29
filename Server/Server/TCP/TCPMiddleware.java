package Server.TCP;

import Server.Common.ResourceManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

        while (true) {

            try {
                Request req = (Request) ois.readObject();
                Response res = new Response();

                System.out.println("Received request for " + req.method);

                String method = req.method;

                if (method.contains("Reserve")) {
                    res = handleReserve(req);
                } else if (method.contains("Flight")) {
                    res = sendToServer(flights_ServerHost, socketPort, req);
                } else if (method.contains("Car")) {
                    res = sendToServer(cars_ServerHost, socketPort, req);
                } else if (method.contains("Room")) {
                    res = sendToServer(rooms_ServerHost, socketPort, req);
                } else if (method.contains("Customer")) {
                    res = handleCustomer(req);
                }
                System.out.println("Sending response to client...");
                oos.writeObject(res);
                oos.flush();
            }
            catch (IOException | ClassNotFoundException e) {
                System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
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
    private Response handleReserve(Request request) throws IOException {
        String method = request.method;
        Response response = new Response();

        if (method.contains("Flight")) {
            response = sendToServer(flights_ServerHost, socketPort, request);
            if (response != null && (boolean) response.result) {
                int customerID = Integer.parseInt((String) request.args.get(0));
                int flightNumber = Integer.parseInt((String) request.args.get(1));
                customers.reserveFlight(customerID, flightNumber);
            }
        } else if (method.contains("Car")) {
            response = sendToServer(cars_ServerHost, socketPort, request);
            if (response != null && (boolean) response.result) {
                int customerID = Integer.parseInt((String) request.args.get(0));
                String location = (String) request.args.get(1);
                customers.reserveCar(customerID, location);
            }
        } else if (method.contains("Room")) {
            response = sendToServer(rooms_ServerHost, socketPort, request);
            if (response != null && (boolean) response.result) {
                int customerID = Integer.parseInt((String) request.args.get(0));
                String location = (String) request.args.get(1);
                customers.reserveRoom(customerID, location);
            }
        } else {
            int customerID = Integer.parseInt((String) request.args.get(1));
            Vector<String> flightNumbers = new Vector<String>();
            for (int i = 0; i < request.args.size() - 5; ++i)
            {
                flightNumbers.addElement((String) request.args.get(2+i));
            }
            String location = (String) request.args.get(request.args.size()-3);
            boolean car = Boolean.parseBoolean((String) request.args.get(request.args.size()-2));
            boolean room = Boolean.parseBoolean((String) request.args.getLast());

            for (String flightNumber : flightNumbers) {
                List<Object> args = new ArrayList<>();
                args.add(flightNumber);
                Request req = new Request("QueryFlight", args);

                response = sendToServer(flights_ServerHost, socketPort, req);
                if (response == null) return badResponse();
                if (Integer.parseInt((String) response.result) < 1) return badResponse();
            }
            for (String flightNumber : flightNumbers) {
                List<Object> args = new ArrayList<>();
                args.add(flightNumber);
                Request req = new Request("ReserveFlight", args);

                sendToServer(flights_ServerHost, socketPort, req);
                customers.reserveFlight(customerID, Integer.parseInt(flightNumber));
            }
            List<Object> args = new ArrayList<>();
            args.add(location);

            Request req = new Request("QueryCars", args);
            response = sendToServer(cars_ServerHost, socketPort, req);
            if (response == null) return badResponse();
            if (car && !((boolean) response.result)) {
                response.result = false;
            }
            else {
                req = new Request("ReserveCar", args);
                sendToServer(cars_ServerHost, socketPort, req);
                customers.reserveCar(customerID, location);
                response.result = true;
            }

            req = new Request("QueryRooms", args);
            response = sendToServer(rooms_ServerHost, socketPort, req);
            if (response == null) return badResponse();
            if (room && !((boolean) response.result)) {
                response.result = false;
            }
            else {
                req = new Request("ReserveRoom", args);
                sendToServer(rooms_ServerHost, socketPort, req);
                customers.reserveRoom(customerID, location);
                response.result = true;
            }
        }
        return response;
    }
    private Response badResponse() {
        Response response = new Response();
        response.result = false;

        return response;
    }
    private Response handleCustomer(Request request) throws RemoteException {
        String method = request.method;
        Response response = new Response();

        switch (method) {
            case "AddCustomer": {
                if (request.args.isEmpty()) response.result = customers.newCustomer();
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