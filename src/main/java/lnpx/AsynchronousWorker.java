package lnpx;

public class AsynchronousWorker extends Thread {

    private static Boolean working;

    public static boolean isWorking() {
        synchronized (working) {
            return working;
        }
    }

    public static void setWorking(boolean w) {
        synchronized (working) {
            working = w;
        }
    }

    @Override
    public void run() {
        while (true) {
            round();
            try {
                Thread.sleep((long) (ServerMain.getScrapingPeriod() * 1000));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void round() {
        System.out.println("Scraping now...");
        if(isWorking()){
            return;
        }
        setWorking(true);
        Scraper.scrape();
        ServerMain.articleTextAnalysis();
        ServerMain.setTrendingKeyWords(MongoDBManager.calculateTrendingKeyWords());
        setWorking(false);
        System.out.println("Scraping round ended");
    }

}
