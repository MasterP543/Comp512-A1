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
            socket = new Socket(serverHost, serverPort); // establish a socket with a server using the given port=3017
            String command;
            try {
                BufferedReader bufferedReader = new java.io.BufferedReader(new InputStreamReader(System.in)); // to read user's input

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // open an output stream to the server...
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); // open an input stream from the server...

                System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
                command = bufferedReader.readLine().trim(); // read user's input

                execute(command, oos, ois);

                ois.close();
                oos.close();
            }
            catch (IOException io) {
                System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
                io.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void execute(String command, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        Vector<String> arguments = parse(command);
        Command cmd = Command.fromString((String)arguments.elementAt(0));

        Response response;

        switch (cmd) {
            case AddFlight: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding a new flight ");
                System.out.println("-Flight Number: " + arguments.elementAt(1));
                System.out.println("-Flight Seats: " + arguments.elementAt(2));
                System.out.println("-Flight Price: " + arguments.elementAt(3));

                arguments.removeFirst();
                Request request = new Request("AddFlight", Arrays.asList(arguments.toArray()));

                System.out.println("Sending request to add flight to Middleware...");
                oos.writeObject(request);
                oos.flush();
                try {
                    System.out.println("Handling response from Middleware...");
                    response = (Response) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.out.println("Flight could not be added");
                    break;
                }

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
                Request request = new Request("addCars", Arrays.asList(arguments.toArray()));

                oos.writeObject(request);
                try {
                    response = (Response) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.out.println("Car could not be added");
                    break;
                }

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
                Request request = new Request("addRooms", Arrays.asList(arguments.toArray()));

                oos.writeObject(request);
                try {
                    response = (Response) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.out.println("Room could not be added");
                    break;
                }

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
                Request request = new Request("addCustomer", Arrays.asList(arguments.toArray()));

                oos.writeObject(request);
                try {
                    response = (Response) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.out.println("Customer could not be added");
                    break;
                }

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

                oos.writeObject(request);
                try {
                    response = (Response) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.out.println("Customer could not be added");
                    break;
                }

                if ((boolean) response.result) {
                    System.out.println("Add customer ID: " + arguments.elementAt(1));
                } else {
                    System.out.println("Customer could not be added");
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
