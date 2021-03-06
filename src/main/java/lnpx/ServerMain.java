package lnpx;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    private static LinkedHashMap<String, Long> trendingKeyWords;
    private static Double scrapingPeriod; //minutes

    static {
        scrapingPeriod = 30.0;
    }
    
    static class closeDBConnections extends Thread {

      @Override
      public void run() {
         MongoDBManager.closeDB();
      }
   }
    public static void main(String[] args) {

        //close db on exit
        Runtime.getRuntime().addShutdownHook(new closeDBConnections());

        //create indexes
        MongoDBManager.createIndexes();

        //create admin
        MongoDBManager.insertUser(new User("admin", "admin", "admin", new Date(), "admin@example.com", "admin", true));

        //update trendingKeyWords for the first time
        trendingKeyWords = MongoDBManager.calculateTrendingKeyWords();

        //Start accepting requests
        ServerRequestListener listenerThread = new ServerRequestListener();
        listenerThread.setDaemon(true);
        listenerThread.start();

        //Start scraping activity
        ServerAsynchronousWorker worker = new ServerAsynchronousWorker();
        worker.setDaemon(true);
        worker.start();

        try {
            listenerThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void setTrendingKeyWords(LinkedHashMap<String, Long> trendingKeyWords) {
        synchronized (trendingKeyWords) {
            ServerMain.trendingKeyWords = trendingKeyWords;
        }
    }

    public static LinkedHashMap<String, Long> getTrendingKeyWords() {
        synchronized (trendingKeyWords) {
            return trendingKeyWords;
        }
    }

    public static double getScrapingPeriod() {
        synchronized (scrapingPeriod) {
            return scrapingPeriod;
        }
    }

    public static void setScrapingPeriod(double period) {
        synchronized (scrapingPeriod) {
            scrapingPeriod = period;
        }
    }

    public static int scrapeNow() {
        if (ServerAsynchronousWorker.isWorking()) {
            return 1;
        }
        ServerAsynchronousWorker.round();
        return 0;
    }

    public static void articleTextAnalysis() {
        ArrayList<Article> art = MongoDBManager.findArticlesNoKeywords();
        String textToAnalyze;
        for (int i = 0; i < art.size(); i++) {
            textToAnalyze = art.get(i).Title + " " + art.get(i).Text;
            textToAnalyze = textToAnalyze.toLowerCase();
            Map<String, Integer> KA = TextAnalyzer.keywordAnalysis(textToAnalyze/*art.get(i).Title + " " +art.get(i).Text*/);
            if (KA != null) {
                MongoDBManager.insertKeywordAnalysis(art.get(i), KA);
            }
        }
    }
}
