package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

public class Middleware implements IResourceManager {

    protected String m_name;
    protected IResourceManager flightsStub;
    protected IResourceManager carsStub;
    protected IResourceManager roomsStub;
    protected ResourceManager customers;

    public Middleware(String p_name, IResourceManager flightsStub, IResourceManager carsStub, IResourceManager roomsStub, ResourceManager customers
    ) {
        this.m_name = p_name;
        this.flightsStub = flightsStub;
        this.carsStub = carsStub;
        this.roomsStub = roomsStub;
        this.customers = customers;
    }

    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightsStub.addFlight(flightNum, flightSeats, flightPrice);

    }

    @Override
    public boolean addCars(String location, int numCars, int price) throws RemoteException {
        return carsStub.addCars(location, numCars, price);
    }

    @Override
    public boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        return roomsStub.addRooms(location, numRooms, price);
    }

    @Override
    public int newCustomer() throws RemoteException {
        return customers.newCustomer();
    }

    @Override
    public boolean newCustomer(int cid) throws RemoteException {
        return customers.newCustomer(cid);
    }

    @Override
    public boolean deleteFlight(int flightNum) throws RemoteException {
        return flightsStub.deleteFlight(flightNum);
    }

    @Override
    public boolean deleteCars(String location) throws RemoteException {
        return carsStub.deleteCars(location);
    }

    @Override
    public boolean deleteRooms(String location) throws RemoteException {
        return roomsStub.deleteRooms(location);
    }

    @Override
    public boolean deleteCustomer(int customerID) throws RemoteException {
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        RMHashMap reservations = customer.getReservations();
        for (String reservedKey : reservations.keySet())
        {
            ReservedItem reserveditem = customer.getReservedItem(reservedKey);
            Trace.info("RM::deleteCustomer(" + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
            String key = reserveditem.getKey();
            int count = reserveditem.getCount();
            String classType = reserveditem.toString();
            if (classType.contains("flight")) {
                flightsStub.removeReservationFlight(key, count);
            }
            if (classType.contains("car")) {
                carsStub.removeReservationCar(key, count);
            }
            if (classType.contains("room")) {
                roomsStub.removeReservationRoom(key, count);
            }
        }
        customers.removeData(customer.getKey());
        Trace.info("RM::deleteCustomer(" + customerID + ") succeeded");
        return true;
    }

    @Override
    public int queryFlight(int flightNumber) throws RemoteException {
        return flightsStub.queryFlight(flightNumber);
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        return carsStub.queryCars(location);
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        return roomsStub.queryRooms(location);
    }

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        return customers.queryCustomerInfo(customerID);
    }

    @Override
    public int queryFlightPrice(int flightNumber) throws RemoteException {
        return flightsStub.queryFlightPrice(flightNumber);
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        return carsStub.queryCarsPrice(location);
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return roomsStub.queryRoomsPrice(location);
    }

    @Override
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        String key = Flight.getKey(flightNumber);
        String location = String.valueOf(flightNumber);
        int price = queryFlightPrice(flightNumber);

        // Read a customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (verifyCustomer(customerID, customer, key, location)) return false;

        // Check if a flight is available
        if (flightsStub.reserveFlight(customerID, flightNumber)) {
            // Reserve flight and update customer object
            reserve(customer, key, location, price);
            return true;
        }
        return false;
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        String key = Car.getKey(location);
        int price = queryCarsPrice(location);

        // Read a customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (verifyCustomer(customerID, customer, key, location)) return false;

        // Check if a car is available
        if (carsStub.reserveCar(customerID, location)) {
            // Reserve car and update customer object
            reserve(customer, key, location, price);
            return true;
        }
        return false;
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        String key = Room.getKey(location);
        int price = queryRoomsPrice(location);

        // Read a customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (verifyCustomer(customerID, customer, key, location)) return false;

        // Check if a room is available
        if (roomsStub.reserveRoom(customerID, location)) {
            // Reserve room and update customer object
            reserve(customer, key, location, price);
            return true;
        }
        return false;
    }

    @Override
    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (verifyCustomer(customerID, customer, "bundle", location)) return false;

        // Check for valid flight numbers
        Vector<Integer> parsedFlights = new Vector<>(flightNumbers.size());
        for (String fn : flightNumbers) {
            try {
                parsedFlights.add(Integer.parseInt(fn));
            } catch (NumberFormatException e) {
                return false; // invalid flight number
            }
        }

        // Check availability for flights
        for (int flightNum : parsedFlights) {
            if (flightsStub.queryFlight(flightNum) < 1) {
                return false;
            }
        }

        // Check car availability
        if (car && carsStub.queryCars(location) < 1) return false;

        // Check room availability
        if (room && roomsStub.queryRooms(location) < 1) return false;

        // Reserve flights
        for (int flightNum : parsedFlights) {
            String key = Flight.getKey(flightNum);
            int price = queryFlightPrice(flightNum);

            flightsStub.reserveFlight(customerID, flightNum);

            reserve(customer, key, location, price);
        }

        // Reserve a car if needed
        if (car) {
            if (!carsStub.reserveCar(customerID, location)) return false;
            reserve(customer, Car.getKey(location), location, queryCarsPrice(location));
        }

        // Reserve a room if needed
        if (room) {
            if (!roomsStub.reserveRoom(customerID, location)) return false;
            reserve(customer, Room.getKey(location), location, queryRoomsPrice(location));
        }

        return true;
    }

    @Override
    public boolean removeReservationFlight(String key, int count) throws RemoteException {
        return flightsStub.removeReservationFlight(key, count);
    }

    @Override
    public boolean removeReservationCar(String key, int count) throws RemoteException {
        return carsStub.removeReservationCar(key, count);
    }

    @Override
    public boolean removeReservationRoom(String key, int count) throws RemoteException {
        return roomsStub.removeReservationRoom(key, count);
    }

    // Verify a customer object exists
    private static boolean verifyCustomer(int customerID, Customer customer, String key, String location) {
        if (customer == null)
        {
            Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
            return true;
        }
        return false;
    }

    private void reserve(Customer customer, String key, String location, int price) {
        customer.reserve(key, location, price);
        customers.writeData(customer.getKey(), customer);
    }

    @Override
    public String getName() throws RemoteException {
        return m_name;
    }


}
