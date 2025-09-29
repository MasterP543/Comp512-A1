package Client;

import Server.TCP.Request;
import Server.TCP.Response;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class TCPClient {
    private static final int serverPort = 3017;
    private static String serverHost;
    private static String serverName;

    private static Socket socket;

    public static void main(String[] args) {
        serverHost = args[0];
        serverName = args[1];

        try {
            TCPClient client = new TCPClient();
            client.start();
        } catch (Exception e) {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void start() throws IOException {
        while(true) {
            String command;
            socket = new Socket(serverHost, serverPort); // establish a socket with a server using the given port=3017

            BufferedReader bufferedReader = new java.io.BufferedReader(new InputStreamReader(System.in)); // to read user's input
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // open an output stream to the server...
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); // open an input stream from the server...

            try {
                System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
                command = bufferedReader.readLine().trim(); // read user's input

                execute(command, oos, ois);
            }
            catch (IOException io) {
                System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
                io.printStackTrace();
                System.exit(1);
            }

            oos.close();
            ois.close();
            socket.close();
        }
    }

    public void execute(String command, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        Vector<String> arguments = parse(command);
        Command cmd = Command.fromString((String)arguments.elementAt(0));

        Response response;

        switch (cmd) {
            case Help: {
                if (arguments.size() == 1) {
                    System.out.println(Command.description());
                } else if (arguments.size() == 2) {
                    Command l_cmd = Command.fromString((String)arguments.elementAt(1));
                    System.out.println(l_cmd.toString());
                } else {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
                }
                break;
            }
            case AddFlight: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding a new flight ");
                System.out.println("-Flight Number: " + arguments.elementAt(1));
                System.out.println("-Flight Seats: " + arguments.elementAt(2));
                System.out.println("-Flight Price: " + arguments.elementAt(3));

                arguments.removeFirst();
                Request request = new Request("AddFlight", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Flight could not be added");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Flight added");
                } else {
                    System.out.println("Flight could not be added");
                }
                break;
            }
            case AddCars: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding new cars");
                System.out.println("-Car Location: " + arguments.elementAt(1));
                System.out.println("-Number of Cars: " + arguments.elementAt(2));
                System.out.println("-Car Price: " + arguments.elementAt(3));

                arguments.removeFirst();
                Request request = new Request("AddCars", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Car could not be added");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Cars added");
                } else {
                    System.out.println("Cars could not be added");
                }
                break;
            }
            case AddRooms: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding new rooms");
                System.out.println("-Room Location: " + arguments.elementAt(1));
                System.out.println("-Number of Rooms: " + arguments.elementAt(2));
                System.out.println("-Room Price: " + arguments.elementAt(3));

                arguments.removeFirst();
                Request request = new Request("AddRooms", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Room could not be added");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Rooms added");
                } else {
                    System.out.println("Rooms could not be added");
                }
                break;
            }
            case AddCustomer: {
                checkArgumentsCount(1, arguments.size());

                System.out.println("Adding a new customer:=");

                arguments.removeFirst();
                Request request = new Request("AddCustomer", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Customer could not be added");
                if (response == null) break;

                int customer = (int) response.result;

                System.out.println("Add customer ID: " + customer);
                break;
            }
            case AddCustomerID: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Adding a new customer");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("newCustomer", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Customer could not be added");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Add customer ID: " + arguments.elementAt(1));
                } else {
                    System.out.println("Customer could not be added");
                }
                break;
            }
            case DeleteFlight: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting a flight");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("DeleteFlight", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Flight could not be deleted");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Flight Deleted");
                } else {
                    System.out.println("Flight could not be deleted");
                }
                break;
            }
            case DeleteCars: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting all cars at a particular location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("DeleteCars", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Cars could not be deleted");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Cars Deleted");
                } else {
                    System.out.println("Cars could not be deleted");
                }
                break;
            }
            case DeleteRooms: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting all rooms at a particular location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("DeleteRooms", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Rooms could not be deleted");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Rooms Deleted");
                } else {
                    System.out.println("Rooms could not be deleted");
                }
                break;
            }
            case DeleteCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting a customer from the database");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("DeleteCustomer", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Customer could not be deleted");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Customer Deleted");
                } else {
                    System.out.println("Customer could not be deleted");
                }
                break;
            }
            case QueryFlight: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying a flight");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryFlight", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Flight could not be queried");
                if (response == null) break;

                int seats = (int) response.result;
                System.out.println("Number of seats available: " + seats);
                break;
            }
            case QueryCars: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying cars location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryCars", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Cars could not be queried");
                if (response == null) break;

                int numCars = (int) response.result;
                System.out.println("Number of cars at this location: " + numCars);
                break;
            }
            case QueryRooms: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying rooms location");
                System.out.println("-Room Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryRooms", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Rooms could not be queried");
                if (response == null) break;

                int numRoom = (int) response.result;
                System.out.println("Number of rooms at this location: " + numRoom);
                break;
            }
            case QueryCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying customer information");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryCustomer", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Customer could not be queried");
                if (response == null) break;

                String bill = (String) response.result;
                System.out.print(bill);
                break;
            }
            case QueryFlightPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying a flight price");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryFlightPrice", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Flight could not be queried");
                if (response == null) break;

                int price = (int) response.result;
                System.out.println("Price of a seat: " + price);
                break;
            }
            case QueryCarsPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying cars price");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryCarsPrice", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Cars could not be queried");
                if (response == null) break;

                int price = (int) response.result;
                System.out.println("Price of cars at this location: " + price);
                break;
            }
            case QueryRoomsPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying rooms price");
                System.out.println("-Room Location: " + arguments.elementAt(1));

                arguments.removeFirst();
                Request request = new Request("QueryRoomsPrice", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Rooms could not be queried");
                if (response == null) break;

                int price = (int) response.result;
                System.out.println("Price of rooms at this location: " + price);
                break;
            }
            case ReserveFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving seat in a flight");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                arguments.removeFirst();
                Request request = new Request("ReserveFlight", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Flight could not be reserved");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Flight Reserved");
                } else {
                    System.out.println("Flight could not be reserved");
                }
                break;
            }
            case ReserveCar: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving a car at a location");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Car Location: " + arguments.elementAt(2));

                arguments.removeFirst();
                Request request = new Request("ReserveCar", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Car could not be reserved");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Car Reserved");
                } else {
                    System.out.println("Car could not be reserved");
                }
                break;
            }
            case ReserveRoom: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving a room at a location");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Room Location: " + arguments.elementAt(2));

                arguments.removeFirst();
                Request request = new Request("ReserveRoom", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Room could not be reserved");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Room Reserved");
                } else {
                    System.out.println("Room could not be reserved");
                }
                break;
            }
            case Bundle: {
                if (arguments.size() < 6) {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 6 arguments. Location \"help\" or \"help,<CommandName>\"");
                    break;
                }

                System.out.println("Reserving an bundle");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                for (int i = 0; i < arguments.size() - 5; ++i)
                {
                    System.out.println("-Flight Number: " + arguments.elementAt(2+i));
                }
                System.out.println("-Location for Car/Room: " + arguments.elementAt(arguments.size()-3));
                System.out.println("-Book Car: " + arguments.elementAt(arguments.size()-2));
                System.out.println("-Book Room: " + arguments.elementAt(arguments.size()-1));

                arguments.removeFirst();
                Request request = new Request("Bundle", Arrays.asList(arguments.toArray()));

                response = sendRequest(oos, request, ois, "Bundle could not be reserved");
                if (response == null) break;

                if ((boolean) response.result) {
                    System.out.println("Bundle Reserved");
                } else {
                    System.out.println("Bundle could not be reserved");
                }
                break;
            }
            case Quit: {
                checkArgumentsCount(1, arguments.size());

                System.out.println("Quitting client");
                System.exit(0);
            }
        }
    }

    private static Response sendRequest(ObjectOutputStream oos, Request request, ObjectInputStream ois, String error) throws IOException {
        Response response;
        oos.writeObject(request);
        oos.flush();
        try {
            response = (Response) ois.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.out.println(error);
            return null;
        }
        return response;
    }

    public static Vector<String> parse(String command)
    {
        Vector<String> arguments = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(command,",");
        String argument = "";
        while (tokenizer.hasMoreTokens())
        {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }

    public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException
    {
        if (expected != actual)
        {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
        }
    }

    public static int toInt(String string) throws NumberFormatException
    {
        return (Integer.valueOf(string)).intValue();
    }

    public static boolean toBoolean(String string)// throws Exception
    {
        return (Boolean.valueOf(string)).booleanValue();
    }
}
