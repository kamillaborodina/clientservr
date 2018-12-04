package apps;


import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {


    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private String server, username;
    private int port;


    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }


    public boolean start() {
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new listen().start();
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }


    private void display(String msg) {

        System.out.println(msg);

    }

    void sendMessage(String msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }


    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}

    }


    public static void main(String[] args) {
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName;
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the username: ");
        userName = scan.nextLine();

        switch(args.length) {
            case 3:
                serverAddress = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                return;
        }
        Client client = new Client(serverAddress, portNumber, userName);
        if(!client.start())
            return;

        while(true) {
            String message = scan.nextLine();
            client.sendMessage(message);

        }
    }

    class listen extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                }
                catch(IOException e) {
                    display(  "Server has closed the connection: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}

