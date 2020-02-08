package lnpx;

import java.util.*;
import java.util.logging.Level;

public class ServerMain {

    private static Map<String, Integer> trendingKeyWords;
    private static Double scrapingPeriod;

    public static void main(String[] args) {

        //create indexes
        MongoDBManager.createIndexes();

        //update trendingKeyWords for the first time
        trendingKeyWords = MongoDBManager.calculateTrendingKeyWords();

        //Start accepting requests
        ServerRequestListener listenerThread = new ServerRequestListener();
        listenerThread.setDaemon(true);
        listenerThread.run();

        //Start scraping activity
        AsynchronousWorker worker = new AsynchronousWorker();
        worker.setDaemon(true);
        worker.run();

    }

    public static void setTrendingKeyWords(Map<String, Integer> trendingKeyWords) {
        synchronized (trendingKeyWords) {
            ServerMain.trendingKeyWords = trendingKeyWords;
        }
    }

    public static Map<String, Integer> getTrendingKeyWords() {
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
        if (AsynchronousWorker.isWorking()) {
            return 1;
        }

        AsynchronousWorker worker = new AsynchronousWorker();
        worker.round();
        return 0;
    }

    public static void articleTextAnalysis() {
        Filters fi = new Filters();
        ArrayList<Article> art = MongoDBManager.findArticles(fi);
        for (int i = 0; i < art.size(); i++) {
            try {
                MongoDBManager.insertKeywordAnalysis(art.get(i), TextAnalyzer.keywordAnalysis(art.get(i).Text));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        MongoDBManager.calculateTrendingKeyWords();
    }
}
