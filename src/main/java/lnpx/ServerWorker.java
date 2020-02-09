package lnpx;

import java.io.*;
import java.net.*;
import java.util.*;
import lnpx.messages.*;

public class ServerWorker extends Thread {

    private User user;
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ServerWorker(Socket sock) {
        socket = sock;
    }

    public User getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

    private void signIn() {
        try {
            SignInMsg msg = (SignInMsg) ois.readObject();
            User u = new User(msg.username, msg.firstName, msg.lastName, msg.dateOfBirth, msg.email, msg.password, msg.adminStatus);
            boolean ret = MongoDBManager.insertUser(u);
            int code = ret ? 0 : -1;

            SignInResponseMsg responseMsg = new SignInResponseMsg(code);
            send("SIGN_IN_R", responseMsg);

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void login() {
        try {
            LoginMsg msg = (LoginMsg) ois.readObject();

            User u = MongoDBManager.userAuthentication(msg.getUsername(), msg.getPassword());
            if (u == null) {
                send("LOGIN_R", new LoginResponseMsg(-1));
                return;
            }

            user = u;
            if (!u.adminStatus) {
                send("LOGIN_R", new LoginResponseMsg(0));
            } else {
                send("LOGIN_R", new LoginResponseMsg(1));
            }

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void trend() {
        send("TREND_R", new TrendResponseMsg(ServerMain.getTrendingKeyWords()));
    }

    private void recommended() {
        List<Article> recommendedArticles = MongoDBManager.suggestedArticles(user);
        send("RECOMMENDED_R", new ArticlesResponseMsg(recommendedArticles));
    }

    private void find() {
        try {
            FindMsg msg = (FindMsg) ois.readObject();

            String keyword = null;
            String topic = null;
            String author = null;
            String newspaper = null;
            String country = null;
            String region = null;
            String city = null;

            Map<String, String> filtersMap = msg.getFilters();

            if (filtersMap.containsKey(topic)) {
                topic = filtersMap.get("topic");
            }
            if (filtersMap.containsKey(author)) {
                author = filtersMap.get("author");
            }
            if (filtersMap.containsKey(newspaper)) {
                newspaper = filtersMap.get("newspaper");
            }
            if (filtersMap.containsKey(country)) {
                country = filtersMap.get("country");
            }
            if (filtersMap.containsKey(region)) {
                region = filtersMap.get("region");
            }
            if (filtersMap.containsKey(city)) {
                city = filtersMap.get("city");
            }

            Filters filters = new Filters(keyword, topic, author, newspaper, country, region, city);
            send("SEARCH_R", new ArticlesResponseMsg(MongoDBManager.findArticles(filters)));

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void view() {
        try {
            ViewMsg msg = (ViewMsg) ois.readObject();

            String keyword = null;
            String topic = null;
            String author = null;
            String newspaper = null;
            String country = null;
            String region = null;
            String city = null;

            Map<String, String> filtersMap = msg.getFilters();

            if (filtersMap.containsKey(topic)) {
                topic = filtersMap.get("topic");
            }
            if (filtersMap.containsKey(author)) {
                author = filtersMap.get("author");
            }
            if (filtersMap.containsKey(newspaper)) {
                newspaper = filtersMap.get("newspaper");
            }
            if (filtersMap.containsKey(country)) {
                country = filtersMap.get("country");
            }
            if (filtersMap.containsKey(region)) {
                region = filtersMap.get("region");
            }
            if (filtersMap.containsKey(city)) {
                city = filtersMap.get("city");
            }

            Filters filters = new Filters(keyword, topic, author, newspaper, country, region, city);
            //get current time
            Date timestamp = new Date();

            MongoDBManager.insertView(new View(user.userID, msg.getLinkArticle(), timestamp, filters));
            send("ACK", new ACKMsg(""));

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void clients() {

        List<User> users = new ArrayList<>();
        users.addAll(MongoDBManager.retrieveUsersInformation().keySet());
        send("CLIENTS_R", new ClientsResponseMsg(users));

    }

    private void changePeriod() {
        try {
            ChangePeriodMsg msg = (ChangePeriodMsg) ois.readObject();
            ServerMain.setScrapingPeriod(msg.getPeriod());
            send("ACK", new ACKMsg(""));

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void scrapeNow() {
        ServerMain.scrapeNow();
        send("ACK", new ACKMsg(""));
    }

    private void changeSites() {
        try {
            ChangeSitesMsg msg = (ChangeSitesMsg) ois.readObject();
            List<String> accounts = new ArrayList<>();
            for(String key : msg.getSites().keySet()){
                if(msg.getSites().get(key)){
                    accounts.add(key);
                }
            }
            Scraper.setAccountToScrape(accounts);
            send("ACK", new ACKMsg(""));
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void send(String cmd, Serializable msg) {//06
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
                    case "FIND":
                        find();
                        break;
                    case "VIEW":
                        view();
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
