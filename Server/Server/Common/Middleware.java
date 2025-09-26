package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

public class Middleware implements IResourceManager {

    protected String m_name = "";
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

    @Override
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
        return customers.deleteCustomer(customerID);
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
        return roomsStub.queryCars(location);
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

        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
            return false;
        }

        if (flightsStub.reserveFlight(customerID, flightNumber)) {
            customer.reserve(key, location, price);
            customers.writeData(customer.getKey(), customer);
            return true;
        }
        return false;
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        String key = Car.getKey(location);
        int price = queryCarsPrice(location);

        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
            return false;
        }

        if (carsStub.reserveCar(customerID, location)) {
            customer.reserve(key, location, price);
            customers.writeData(customer.getKey(), customer);
            return true;
        }
        return false;
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        String key = Room.getKey(location);
        int price = queryRoomsPrice(location);

        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer) customers.readData(Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
            return false;
        }

        if (roomsStub.reserveRoom(customerID, location)) {
            customer.reserve(key, location, price);
            customers.writeData(customer.getKey(), customer);
            return true;
        }
        return false;
    }

    @Override
    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        return false;
    }

    @Override
    public String getName() throws RemoteException {
        return m_name;
    }
}
