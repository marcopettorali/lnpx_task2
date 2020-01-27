package lnpx;

import java.util.*;

public class ServerMain {

    private static Map<String, Integer> trendingKeyWords;
    private static Double scrapingPeriod;

    private static void main(String[] args) {

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
}
