package lnpx;

import java.io.*;
import java.net.*;
import lnpx.messages.*;

public class ServerWorker extends Thread {

    private String username;
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ServerWorker(Socket sock) {
        socket = sock;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }
    
     private void signIn() {
        try {
            SignInMsg msg = (SignInMsg) ois.readObject();
            //[...] insert code here
            //boolean insertUser(User)

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void login() {
        try {
            LoginMsg msg = (LoginMsg) ois.readObject();
            //[...] insert code here
            //(User|null) userAuthentication(userId, password)

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void trend() {
        //[...] insert code here
        //Map<String, double> calculateTrendingKeyWord()
    }

    private void recommended() {
        //[...] insert code here
        //List<Article> suggestedArticles(User)
    }

    private void search() {
        try {
            SearchMsg msg = (SearchMsg) ois.readObject();
            //[...] insert code here
            //insertSearch(Search)
            //List<Article> findArticles(Filters)

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void clients() {
       //[...] insert code here
       //TODO!!!!
    }

    private void changePeriod() {
        try {
            ChangePeriodMsg msg = (ChangePeriodMsg) ois.readObject();
            //[...] insert code here
            //TODO!!!

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void scrapeNow() {
        //[...] insert code here
    }

    private void changeSites() {
        try {
            ChangeSitesMsg msg = (ChangeSitesMsg) ois.readObject();
            //[...] insert code here

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void send(String cmd, Serializable msg) {//06
        synchronized (socket) {
            try {
                dos.writeUTF(cmd);
                oos.writeObject(msg);
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        while (true) {
            try {
                String cmd = dis.readUTF();
                switch (cmd) {
                    case "SIGN_IN":
                        signIn();
                        break;
                    case "LOGIN":
                        login();
                        break;
                    case "TREND":
                        trend();
                        break;
                    case "RECOMMENDED":
                        recommended();
                        break;
                    case "SEARCH":
                        search();
                        break;
                    case "CLIENTS":
                        clients();
                        break;
                    case "CHANGE_PERIOD":
                        changePeriod();
                        break;
                    case "SCRAPE_NOW":
                        scrapeNow();
                        break;
                    case "CHANGE_SITES":
                        changeSites();
                        break;
                    default:
                        System.out.println("ERROR: command '" + cmd + "' not found: ignoring.");
                        break;
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
