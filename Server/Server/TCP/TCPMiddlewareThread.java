package Server.TCP;

import Server.Common.*;

import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TCPMiddlewareThread extends Thread{

    Socket socket;
    private static final int socketPort = 3017;
    private String flights_ServerHost;
    private String cars_ServerHost;
    private String rooms_ServerHost;

    private static ResourceManager customers;

    public TCPMiddlewareThread (String flights_ServerHost, String cars_ServerHost, String rooms_ServerHost, Socket socket, ResourceManager customers) {
        this.flights_ServerHost = flights_ServerHost;
        this.cars_ServerHost = cars_ServerHost;
        this.rooms_ServerHost = rooms_ServerHost;
        this.customers = customers;
        this.socket = socket;
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

                if (method.contains("Reserve")) {
                    res = handleReserve(req);
                } else if (method.contains("Bundle")) {
                    res = handleBundle(req);
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

    private Response handleReserve(Request request) throws IOException {
        String method = request.method;
        Response response = new Response();

        if (method.contains("Flight")) {
            int customerID = (int) request.args.get(0);
            int flightNumber = (int) request.args.get(1);
            response.result = reserveFlight(customerID, flightNumber);
        } else if (method.contains("Car")) {
            response = sendToServer(cars_ServerHost, socketPort, request);
            if (response != null && (boolean) response.result) {
                int customerID = (int) request.args.get(0);
                String location = (String) request.args.get(1);
                response.result = reserveCar(customerID, location);
            }
        } else if (method.contains("Room")) {
            response = sendToServer(rooms_ServerHost, socketPort, request);
            if (response != null && (boolean) response.result) {
                int customerID = (int) request.args.get(0);
                String location = (String) request.args.get(1);
                response.result = reserveRoom(customerID, location);
            }
        }
        return response;
    }
    private synchronized Response handleBundle(Request request) throws IOException {
        Response response = new Response();

        int customerID = Integer.parseInt((String) request.args.get(0));
        Vector<Integer> flightNumbers = new Vector<Integer>();
        for (int i = 0; i < request.args.size() - 4; i++)
        {
            flightNumbers.addElement(Integer.parseInt((String) request.args.get(1+i)));
        }
        String location = (String) request.args.get(request.args.size()-3);
        boolean car = Boolean.parseBoolean((String) request.args.get(request.args.size()-2));
        boolean room = Boolean.parseBoolean((String) request.args.getLast());
        Vector<Integer> reservedFlights = new Vector<Integer>();

        for (int flightNumber : flightNumbers) {
            List<Object> args = new ArrayList<>();
            args.add(flightNumber);
            Request req = new Request("QueryFlight", args);

            response = sendToServer(flights_ServerHost, socketPort, req);
            if (response == null) return badResponse();
            if (((int) response.result) < 1) return badResponse();
        }

        List<Object> args = new ArrayList<>();
        args.add(location);

        if (car) {
            Request req = new Request("QueryCars", args);
            response = sendToServer(cars_ServerHost, socketPort, req);
            if (response == null) return badResponse();
            if ((int) response.result < 1) {
                response.result = false;
                return response;
            }

            if (!reserveCar(customerID, location)) {
            }
        }

        if (room) {
            Request req = new Request("QueryRooms", args);
            response = sendToServer(rooms_ServerHost, socketPort, req);
            if (response == null) return badResponse();
            if ((int) response.result < 1) {
                response.result = false;
                return response;
            }
        }


        for (int flightNumber : flightNumbers) {
            if (!reserveFlight(customerID, flightNumber)){
                deleteFlightsReservations(customerID, reservedFlights);
                response.result = false;
                return response;
            }
            reservedFlights.add(flightNumber);
        }

        reserveCar(customerID, location);
        reserveRoom(customerID, location);

        response.result = true;

        return response;
    }

    private void deleteFlightsReservations(int customerID, Vector<Integer> reservedFlights) {
        Customer customer = customers.getCustomer(customerID);

        for (int flightNumber : reservedFlights) {
            customer.getReservations().remove(Flight.getKey(flightNumber));
        }

    }

    private Response badResponse() {
        Response response = new Response();
        response.result = false;

        return response;
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

    private Response handleCustomer(Request request) throws IOException {
        String method = request.method;
        Response response = new Response();

        switch (method) {
            case "AddCustomer": {
                response.result = customers.newCustomer();
                break;
            }
            case "AddCustomerID": {
                response.result = customers.newCustomer((int) request.args.getFirst());
                break;
            }
            case "DeleteCustomer": {
                int customerID = (int) request.args.getFirst();
                Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
                RMHashMap reservations = customer.getReservations();
                for (String reservedKey : reservations.keySet())
                {
                    ReservedItem reserveditem = customer.getReservedItem(reservedKey);
                    Trace.info("RM::deleteCustomer(" + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
                    String key = reserveditem.getKey();
                    int count = reserveditem.getCount();
                    Vector<Object> args = new Vector<>();
                    args.add(key);
                    args.add(count);

                    String classType = reserveditem.toString();
                    if (classType.contains("flight")) {
                        Request req = new Request("RemoveReservationFlight", args);
                        response = sendToServer(flights_ServerHost, socketPort, req);
                    }
                    if (classType.contains("car")) {
                        Request req = new Request("RemoveReservationCar", args);
                        response = sendToServer(cars_ServerHost, socketPort, req);
                    }
                    if (classType.contains("room")) {
                        Request req = new Request("RemoveReservationRoom", args);
                        response = sendToServer(rooms_ServerHost, socketPort, req);
                    }
                }
                customers.removeData(customer.getKey());
                Trace.info("RM::deleteCustomer(" + customerID + ") succeeded");
                break;
            }
            case "QueryCustomer": {
                response.result = customers.queryCustomerInfo((int) request.args.getFirst());
                break;
            }
        }
        return response;
    }


    public boolean reserveFlight(int customerID, int flightNumber) throws IOException {
        String key = Flight.getKey(flightNumber);
        String location = String.valueOf(flightNumber);

        // Check if a flight is available and get its price
        Response res = sendToServer(flights_ServerHost, socketPort, new Request("QueryFlightPrice", List.of(flightNumber)));
        int price;
        if (res != null) {
            price = (int) res.result;
        } else {
            return false;
        }

        try {
            if (customers.queryCustomerInfo(customerID).isEmpty()) {
                Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
                return false;
            }
        } catch (RemoteException e) {}

        // Read a customer object if it exists (and read lock it)
        Customer customer = customers.getCustomer(customerID);

        // Reserve the flight
        Request request = new Request("ReserveFlight", List.of(customerID, flightNumber ));
        Response response = sendToServer(flights_ServerHost, socketPort, request);
        if (response != null && (boolean) response.result) {

            // update the customer's reservations
            updateCustomerReservations(customer, key, location, price);
            return true;
        }

        return false;
    }

    public boolean reserveCar(int customerID, String location) throws IOException {
        String key = Car.getKey(location);

        // Check if a car is available and get its price
        Response res = sendToServer(cars_ServerHost, socketPort, new Request("QueryCarsPrice", List.of(location)));
        int price;
        if (res != null) {
            price = (int) res.result;
        } else {
            return false;
        }

        try {
            if (customers.queryCustomerInfo(customerID).isEmpty()) {
                Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
                return false;
            }
        } catch (RemoteException e) {}

        // Read a customer object if it exists (and read lock it)
        Customer customer = customers.getCustomer(customerID);

        // Reserve the Car
        Request request = new Request("ReserveCar", List.of(customerID, location ));
        Response response = sendToServer(cars_ServerHost, socketPort, request);
        if (response != null && (boolean) response.result) {

            // update the customer's reservations
            updateCustomerReservations(customer, key, location, price);

            return true;
        }
        return false;
    }


    public boolean reserveRoom(int customerID, String location) throws IOException {
        String key = Room.getKey(location);

        // Check if a room is available and get its price
        Response res = sendToServer(rooms_ServerHost, socketPort, new Request("QueryRoomsPrice", List.of(location)));
        int price;
        if (res != null) {
            price = (int) res.result;
        } else {
            return false;
        }

        try {
            if (customers.queryCustomerInfo(customerID).isEmpty()) {
                Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
                return false;
            }
        } catch (RemoteException e) {}

        // Read a customer object if it exists (and read lock it)
        Customer customer = customers.getCustomer(customerID);

        // Reserve the Room
        Request request = new Request("ReserveRoom", List.of(customerID, location ));
        Response response = sendToServer(rooms_ServerHost, socketPort, request);
        if (response != null && (boolean) response.result) {
            updateCustomerReservations(customer, key, location, price);
            return true;
        }

        return false;
    }

    public synchronized void updateCustomerReservations(Customer customer, String key, String location, int price){
        customer.reserve(key, location, price);
        customers.updateCustomerReservations(customer);
    }

}
