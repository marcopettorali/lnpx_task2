package lnpx;

public class ServerAsynchronousWorker extends Thread {

    private static Boolean working;
    
    static{
        working = false;
    }

    public static boolean isWorking() {
        synchronized (working) {
            if(working){
                System.out.println("Scraping already working");
            }
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
        System.out.println("ServerAsynchronousWorker built");
        while (true) {
            try {
                Thread.sleep((long) (ServerMain.getScrapingPeriod() * 1000 * 60));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            round();
        }
    }

    public static void round() {
        System.out.println("Scraping now...");
        if (isWorking()) {
            return;
        }
        setWorking(true);
        Scraper.scrape();
        ServerMain.articleTextAnalysis();
        MongoDBManager.deleteArticleNoKeywords();
        ServerMain.setTrendingKeyWords(MongoDBManager.calculateTrendingKeyWords());
        setWorking(false);
        System.out.println("Scraping round ended");
    }

}
