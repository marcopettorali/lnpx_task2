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
        System.out.println(recommendedArticles);
        send("RECOMMENDED_R", new ArticlesResponseMsg(recommendedArticles));
    }

    private void find() {
        try {
            FindMsg msg = (FindMsg) ois.readObject();

            String keyword = "";
            String topic = "";
            String author = "";
            String newspaper = "";
            String country = "";
            String region = "";
            String city = "";

            Map<String, String> filtersMap = msg.getFilters();

            keyword = msg.getKeyword();
           
          if(filtersMap.get("Topic").equals("")){
              topic=null;
          }
          else{
              topic=filtersMap.get("Topic");
          }
          
          if(filtersMap.get("Author").equals("")){
              author=null;
          }
          else{
              author=filtersMap.get("Author");
          }
          
          if(filtersMap.get("Newspaper").equals("")){
              newspaper=null;
          }
          else{
              newspaper=filtersMap.get("Newspaper");
          }
          
          if(filtersMap.get("Country").equals("")){
              country=null;
          }
          else{
              country=filtersMap.get("Country");
          }
          
          if(filtersMap.get("Region").equals("")){
              region=null;
          }
          else{
              region=filtersMap.get("Region");
          }
          
          if(filtersMap.get("City").equals("")){
              city=null;
          }
          else{
              city=filtersMap.get("City");
          }
          
          if(keyword.equals("")){
              keyword=null;
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

            String keyword = "";
            String topic = "";
            String author = "";
            String newspaper = "";
            String country = "";
            String region = "";
            String city = "";

            Map<String, String> filtersMap = msg.getFilters();
            keyword = filtersMap.get("Keyword");
            topic = filtersMap.get("Topic");
            author = filtersMap.get("Author");
            newspaper = filtersMap.get("Newspaper");
            country = filtersMap.get("Country");
            region = filtersMap.get("Region");
            city = filtersMap.get("City");
            if(keyword.equals(""))
            {
                keyword=null;
            }
            
            if(topic.equals(""))
            {
                topic= null;
            }
            if(author.equals("")){
                author = null;
            }
            if(newspaper.equals("")){
                newspaper = null;
            }
            if(country.equals("")){
                country = null;
            }
            if(region.equals("")){
                region = null;
            }
            if(city.equals("")){
                city = null;
            }
            
            Filters filters = new Filters(keyword, topic, author, newspaper, country, region, city);
            Date timestamp = new Date();
            MongoDBManager.insertView(new View(user.userID, msg.getLinkArticle(), timestamp, filters));
            send("ACK", new ACKMsg(""));

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void clients() {

        List<User> users = new ArrayList<>();
        Map<User,Integer> res = MongoDBManager.retrieveUsersInformation();
        for(Map.Entry<User,Integer> entry: res.entrySet()){
            
            entry.getKey().setViews(entry.getValue());
            
        }
        
        
        users.addAll(res.keySet());
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
            for (String key : msg.getSites().keySet()) {
                if (msg.getSites().get(key)) {
                    accounts.add(key);
                }
            }
            Scraper.setAccountToScrape(accounts);
            send("ACK", new ACKMsg(""));
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private void deleteUser(){
        try {
            DeleteUserMsg msg = (DeleteUserMsg) ois.readObject();
            
            MongoDBManager.deleteUser(msg.getUsername());
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
                //System.out.println("Sent " + msg.getClass().getSimpleName() + " to " + user.userID + "@" + socket.getInetAddress().getHostAddress());
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
                System.out.println("Received " + cmd + " from " + socket.getInetAddress().getHostAddress());
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
                    case "DELETE_USER":
                        deleteUser();
                        break;
                    default:
                        System.out.println("ERROR: command '" + cmd + "' not found: ignoring.");
                        break;
                }
            } catch (IOException ex) {
                try {
                    socket.close();
                    System.err.println("Connection closed by " + user.userID + "@" + socket.getInetAddress().getHostAddress());
                    break;
                } catch (IOException ex1) {
                    System.err.println(ex1.getMessage());
                }
            }
        }
    }
}