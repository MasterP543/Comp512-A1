package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

public class Middleware implements IResourceManager {

    protected String m_name = "";
    protected IResourceManager flightsStub;
    protected IResourceManager carsStub;
    protected IResourceManager roomsStub;
    protected IResourceManager customersStub;

    public Middleware(String p_name, IResourceManager flightsStub, IResourceManager carsStub, IResourceManager roomsStub, IResourceManager customerStub
    ) {
        this.m_name = p_name;
        this.flightsStub = flightsStub;
        this.carsStub = carsStub;
        this.roomsStub = roomsStub;
        this.customersStub = customerStub;
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
        return customersStub.newCustomer();
    }

    @Override
    public boolean newCustomer(int cid) throws RemoteException {
        return customersStub.newCustomer(cid);
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
        return customersStub.deleteCustomer(customerID);
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
        return customersStub.queryCustomerInfo(customerID);
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
        return flightsStub.reserveFlight(customerID, flightNumber);
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return carsStub.reserveCar(customerID, location);
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return roomsStub.reserveRoom(customerID, location);
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
