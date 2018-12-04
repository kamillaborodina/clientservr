package apps;


import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
    private static int ID;
    private ArrayList<ClientThread> al;
    private int portNumber;
    private boolean keepGoing;

    public static void main(String[] args) {

        Server server = new Server(1500);
        server.start();
    }

    public Server(int portNumber) {
        this.portNumber = portNumber;
        al = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while(keepGoing)
            {
                print("Waiting for a client... ");
                Socket socket = serverSocket.accept();
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                al.add(t);

                t.start();
            }
            try {
                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }
            catch(Exception e) {
                print("Exception closing the server and clients: " + e);
            }
        }
        catch (IOException e) {
            String msg = " Exception on new ServerSocket: " + e + "\n";
            print(msg);
        }
    }



    private synchronized boolean sendMsg(String message) {
        String s = "";

        String messageLf = s + " " + message + "\n";
        print(messageLf);

        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                print("Disconnected Client " + ct.username + " removed from list.");
            }
        }
        return true;


    }
    synchronized void remove(int id) {

        String disconnectedClient = "";
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                al.remove(i);
                break;
            }
        }
        sendMsg( disconnectedClient + " has left the chat room." );
    }


    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        String out = "";

        ClientThread(Socket socket) {
            id = ++ID;
            this.socket = socket;
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            }
            catch (IOException e) {
                print("Exception creating new Input/output Streams: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
            }
            out =  "\n";
        }

        public String getUsername() {
            return username;
        }

        public void run() {
            String cm;
            while(true) {
                try {
                    cm = (String) sInput.readObject();
                }
                catch (IOException e) {
                    print(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                boolean confirmation =  sendMsg(username + ": " + cm);
                if(!confirmation){
                    String msg =  "Sorry. No such user exists.";
                    writeMsg(msg);
                }
            }
            remove(id);
            close();
        }

        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        private boolean writeMsg(String msg) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(msg);
            }
            catch(IOException e) {
                print("Error sending message to " + username);
                print(e.toString());
            }
            return true;
        }
    }
    private void print(String s) {
        System.out.println(s);
    }
}

