package lnpx;

import org.apache.log4j.Logger;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.*;
import twitter4j.conf.*;

public class Scraper {

    private static String[] accountToScrape = {"repubblica", "Corriere", "Agenzia_Ansa", "SkyTG24"};

    static Logger log = Logger.getLogger(
            Scraper.class.getName());

    //Funzione di utility per estrarre dalla stringa autori di Repubblica una lista di nomi
    private static List<String> getAuthorsRepubblica(String str) {
        List<String> result = new ArrayList<>();
        str = str.replaceAll("di ", "");
        result = Arrays.asList(str.split(" e "));
        return result;
    }
    //Funzione di utility per trasformare la data in formato italiano nel formato corretto

    private static String getStringDate(String str) {
        String result = null;
        String[] splitted = str.split(" ");
        if (splitted.length != 3) {
            return null;
        }
        result = splitted[2];
        switch (splitted[1].toUpperCase()) {
            case "GENNAIO":
                result += "-01-";
                break;
            case "FEBBRAIO":
                result += "-02-";
                break;
            case "MARZO":
                result += "-03-";
                break;
            case "APRILE":
                result += "-04-";
                break;
            case "MAGGIO":
                result += "-05-";
                break;
            case "GIUGNO":
                result += "-06-";
                break;
            case "LUGLIO":
                result += "-07-";
                break;
            case "AGOSTO":
                result += "-08-";
                break;
            case "SETTEMBRE":
                result += "-09-";
                break;
            case "OTTOBRE":
                result += "-10-";
                break;
            case "NOVEMBRE":
                result += "-11-";
                break;
            case "DICEMBRE":
                result += "-12-";
                break;
        }
        result += splitted[0];
        return result;
    }

    //Funzione per estrarre i tag dalla stringa dell'ANSA
    private static List<String> getTagANSA(String str) {
        List<String> result = new ArrayList<>();
        String trimmedString = str.trim();
        String[] splitted = trimmedString.trim().substring(12, trimmedString.length() - 2).replace("\"", "").split(",");
        for (int i = 0; i < splitted.length - 1; i++) {
            result.add(splitted[i]);
        }
        return result;
    }

    private static void scrapeCorriere(String articleLink) {
        try {
            Article article = new Article();
            article.Newspaper = "Corriere della Sera";
            log.info("GIORNALE: Corriere della Sera");
            log.info("LINK: " + articleLink);
            Document doc = Jsoup.connect(articleLink).get();
            article.Link = articleLink;
            //----TITOLO DELL'ARTICOLO----//
            Elements titoloClass = doc.select("[class=\"article-title\"]").not("h4");
            if (!titoloClass.isEmpty()) {
                Element titolo = titoloClass.get(0);
                if (titolo != null) {
                    article.Title = titolo.text();
                    log.info("TITOLO: " + titolo.text());
                } else {
                    log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
                }
            } else {
                titoloClass = doc.getElementsByClass("title is--large");
                if (!titoloClass.isEmpty()) {
                    Element titolo = titoloClass.get(0);
                    if (titolo != null) {
                        article.Title = titolo.text();
                        log.info("TITOLO: " + titolo.text());
                    } else {
                        log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
                    }
                } else {
                    log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
                }
            }
            //----AUTORE DELL'ARTICOLO----//
            Elements autoreClass = doc.getElementsByClass("writer");
            if (!autoreClass.isEmpty()) {
                Element autore = autoreClass.get(0);
                if (autore != null) {
                    List<String> autori = new ArrayList<>();
                    autori.add(autore.text());
                    article.Authors = autori;
                    log.info("AUTORE: " + autore.text());
                } else {
                    log.warn("L'applicazione non è in grado di risalire all'autore dell'articolo. " + articleLink);
                }
            } else {
                autoreClass = doc.getElementsByClass("signature");
                if (!autoreClass.isEmpty() && autoreClass.get(0).is("strong")) {
                    Element autore = autoreClass.get(0);
                    if (autore != null) {
                        List<String> autori = new ArrayList<>();
                        autori.add(autore.text());
                        article.Authors = autori;
                        log.info("AUTORE: " + autore.text());
                    } else {
                        log.warn("L'applicazione non è in grado di risalire all'autore dell'articolo. " + articleLink);
                    }
                } else {
                    log.warn("L'applicazione non è in grado di risalire all'autore dell'artitolo. " + articleLink);
                }
            }
            //----TESTO DELL'ARTICOLO----//
            Elements testoClass = doc.getElementsByClass("chapter-paragraph");
            if (!testoClass.isEmpty()) {
                String testoArticolo = "";
                for (Element testo : testoClass) {
                    testoArticolo += testo.text();
                }
                if (!testoArticolo.equals("")) {
                    log.info("TESTO: " + testoArticolo);
                    article.Text = testoArticolo;
                } else {
                    log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                }
            } else {
                testoClass = doc.getElementsByClass("chapter-description");
                if (!testoClass.isEmpty()) {
                    Element testo = testoClass.get(0);
                    if (testo != null) {
                        log.info("TESTO: " + testo.text());
                        article.Text = testo.text();
                    } else {
                        log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                    }
                } else {
                    testoClass = doc.getElementsByClass("content");
                    if (!testoClass.isEmpty()) {
                        String testoArticolo = "";
                        for (Element testo : testoClass) {
                            testoArticolo += testo.text();
                        }
                        if (!testoArticolo.equals("")) {
                            log.info("TESTO: " + testoArticolo);
                            article.Text = testoArticolo;
                        } else {
                            log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                        }
                    } else {
                        log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                    }
                    log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                }
            }
            //----DATA DELL'ARTICOLO----//
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Elements dataClass = doc.getElementsByClass("article-date-place");
            if (!dataClass.isEmpty()) {
                Element data = dataClass.get(0);
                if (data != null) {
                    String dataString = data.text();
                    String[] split = dataString.split("\\(");
                    try {
                        if (getStringDate(split[0]) != null) {
                            log.info("DATA: " + getStringDate(split[0]));
                            article.Date = df.parse(getStringDate(split[0]));
                        } else {
                            article.Date = new java.util.Date();
                            log.info("DATA: " + new java.util.Date());
                        }
                    } catch (ParseException ex) {
                        java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                    article.Date = new java.util.Date();
                    log.info("DATA: " + new java.util.Date());
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                article.Date = new java.util.Date();
                log.info("DATA: " + new java.util.Date());
            }
            //----TOPIC DELL'ARTICOLO----//
            Elements parents = doc.getElementsByTag("li");
            Elements topicClass = null;
            for (Element parent : parents) {
                if (topicClass == null) {
                    topicClass = parent.select("[class=\"category\"]");
                } else {
                    topicClass.addAll(parent.select("[class=\"category\"]"));
                }
            }
            if (topicClass != null && !topicClass.isEmpty()) {
                Element topic = topicClass.get(0);
                if (topic != null) {
                    log.info("TOPIC: " + topic.text());
                    List<String> topicList = new ArrayList<>();
                    topicList.add(topic.text());
                    article.Topic = topicList;
                } else {
                    log.warn("L'applicazione non è in grado di risalire ai topic dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire ai topic dell'articolo. " + articleLink);
            }
            if (article.Title != null) {
                MongoDBManager.insertArticle(article);
            }
        } catch (IOException ex) {
            log.warn("URL non valido. " + articleLink);
        }
    }

    private static void scrapeRepubblica(String articleLink) {

        try {
            Article article = new Article();
            article.Newspaper = "La Repubblica";
            log.info("GIORNALE: La Repubblica");
            Document doc = Jsoup.connect(articleLink).get();
            log.info("LINK: " + articleLink);
            article.Link = articleLink;
            //Estrazione del titolo dell'articolo:
            Elements titles = doc.select("[itemprop=headline name]").not("span");
            if (!titles.isEmpty()) {
                Element title = titles.get(0);
                if (title != null) {
                    log.info("TITOLO: " + title.text());
                }
                article.Title = title.text();
            } else {
                titles = doc.select("[itemprop=name]").not("span");
                if (!titles.isEmpty()) {
                    Element title = titles.get(0);
                    if (title != null) {
                        log.info("TITOLO: " + title.text());
                    }
                    article.Title = title.text();
                } else {
                    Elements titleClasses = doc.getElementsByClass("shared-detail mini-apertura");
                    if (!titleClasses.isEmpty()) {
                        Element titleClass = titleClasses.get(0);
                        titles = titleClass.getElementsByTag("h1");
                        if (!titles.isEmpty()) {
                            Element title = titles.get(0);
                            if (title != null) {
                                log.info("TITOLO: " + title.text());
                            }
                            article.Title = title.text();
                        }
                    } else {
                        log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);

                    }
                }
            }
            //Estrazione dell'autore dell'articolo:
            Elements classAuthors = doc.getElementsByClass("author");
            if (!classAuthors.isEmpty()) {
                Element classAuthor = classAuthors.get(0);
                Elements authors = classAuthor.select("[itemprop=name]");
                if (!authors.isEmpty()) {
                    Element authorText = authors.get(0);
                    if (authorText != null) {
                        List<String> authorList = getAuthorsRepubblica(authorText.text());
                        article.Authors = authorList;
                        for (String author : authorList) {
                            log.info("AUTORE: " + author);
                        }
                    }
                } else {
                    log.warn("L'applicazione non è riuscita a risalire all'autore dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è riuscita a risalire all'autore dell'articolo. " + articleLink);
            }
            //Estrazione della data dell'articolo
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Elements dates = doc.select("[itemprop=uploadDate]");
            if (!dates.isEmpty()) {
                Element date = dates.get(0);
                if (date != null) {
                    log.info("DATA :" + date.attr("datetime"));
                    try {
                        article.Date = df.parse(date.attr("datetime"));
                    } catch (ParseException ex) {
                        log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                        article.Date = new java.util.Date();
                        log.info("DATA: " + new java.util.Date());
                    }
                }
            } else {
                dates = doc.select("[itemprop=datePublished]");
                if (!dates.isEmpty()) {
                    Element date = dates.get(0);
                    if (date != null) {
                        log.info("DATA : " + date.attr("datetime"));
                        try {
                            article.Date = df.parse(date.attr("datetime"));
                        } catch (ParseException ex) {
                            log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                            article.Date = new java.util.Date();
                            log.info("DATA: " + new java.util.Date());
                        }
                    }
                } else {
                    dates = doc.getElementsByTag("time");
                    if (!dates.isEmpty()) {
                        Element date = dates.get(0);
                        if (date != null) {
                            log.info("DATA : " + date.text());
                            try {
                                article.Date = df.parse(getStringDate(date.text()));
                            } catch (ParseException ex) {
                                log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                                article.Date = new java.util.Date();
                                log.info("DATA: " + new java.util.Date());
                            }
                        }
                    }
                    log.info("L'applicazione non è riuscita a risalire alla data dell'articolo. " + articleLink);
                }
            }
            //Estrazione del testo dell'articolo:
            Elements articleBodys = doc.select("[itemprop=articleBody]");
            if (!articleBodys.isEmpty()) {
                Element articleBody = articleBodys.get(0);
                if (articleBody != null) {
                    log.info("ARTICOLO: " + articleBody.text());
                    article.Text = articleBody.text();
                }
            } else {
                articleBodys = doc.select("[itemprop=description]");
                if (!articleBodys.isEmpty()) {
                    Element articleBody = articleBodys.get(0);
                    if (articleBody != null) {
                        log.info("ARTICOLO: " + articleBody.text());
                        article.Text = articleBody.text();
                    }
                } else {
                    log.warn("L'applicazione non è in grado di risalire al corpo dell'articolo. " + articleLink);
                }
            }
            Element arg = null;
            Elements args = doc.getElementsByClass("args");
            if (!args.isEmpty()) {
                arg = args.get(0);
            } else {
                log.warn("L'applicazione non è in grado di risalire agli argomenti dell'articolo. " + articleLink);
            }

            if (arg != null) {
                Elements dds = arg.getElementsByTag("dd");
                if (!dds.isEmpty()) {
                    List<String> Tags = new ArrayList<>();
                    for (Element dd : dds) {
                        if (dd != null) {
                            log.info("TAG: " + dd.text());
                            Tags.add(dd.text());
                        }
                    }
                    article.Topic = Tags;
                } else {
                    log.warn("L'applicazione non è in grado di risalire ai tags dell'articolo. " + articleLink);
                }
            }
            //L'applicazione scarta gli articoli di Rep che sono gli articoli a pagamento della repubblica
            if (article.Title != null) {
                MongoDBManager.insertArticle(article);
            }
        } catch (IOException ex) {
            log.warn("URL non valido. " + articleLink);
        }
    }

    private static void scrapeAnsa(String articleLink) {
        try {

            Article article = new Article();
            article.Newspaper = "ANSA";
            log.info("GIORNALE: ANSA");
            Document doc = Jsoup.connect(articleLink).get();
            log.info("LINK: " + articleLink);
            article.Link = articleLink;
            //----TITOLO----//
            Elements titoloClass = doc.getElementsByClass("news-title");
            if (!titoloClass.isEmpty()) {
                String Titolo = "";
                for (Element e : titoloClass) {
                    if (e.is("h1")) {
                        Titolo = e.text();
                    }
                }
                if (Titolo.equals("")) {
                    log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
                } else {
                    log.info("TITOLO: " + Titolo);
                    article.Title = Titolo;
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo");
            }
            //----AUTORE----//
            Elements autoreClass = doc.getElementsByClass("news-author");
            if (!autoreClass.isEmpty()) {
                Element autore = autoreClass.get(0);
                if (autore != null) {
                    List<String> autori = new ArrayList<>();
                    autori.add(autore.text());
                    article.Authors = autori;
                    log.info("AUTORE: " + autore.text());
                } else {
                    log.warn("L'applicazione non è in grado di risalire all'autore dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire all'autore dell'articolo. " + articleLink);
            }
            //----LOCATION----//
            Elements locationClass = doc.getElementsByClass("location");
            if (!locationClass.isEmpty()) {
                Element location = locationClass.get(0);
                if (location != null) {
                    log.info("LOCATION: " + location.text());
                    article.City = location.text();
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire alla locazione dell'articolo. " + articleLink);
            }
            //----DATA DELL'ARTICOLO----//
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Elements dataClass = doc.getElementsByClass("news-time");
            if (!dataClass.isEmpty()) {
                Element data = dataClass.get(0);
                for (Element d : data.children()) {
                    if (d.is("em") || d.is("strong")) {
                        try {
                            article.Date = df.parse(getStringDate(d.text()));
                            log.info("DATA: " + getStringDate(d.text()));
                        } catch (ParseException ex) {
                            log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                            article.Date = new java.util.Date();
                            log.info("DATA: " + new java.util.Date());
                        }

                    }
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire alla data dell'articolo. " + articleLink + "data impostata ad oggi.");
                article.Date = new java.util.Date();
                log.info("DATA: " + new java.util.Date());
            }
            //----TESTO DELL'ARTICOLO----//
            Elements testoClass = doc.getElementsByClass("news-txt");
            if (!testoClass.isEmpty()) {
                Element testo = testoClass.get(0);
                if (testo != null) {
                    article.Text = testo.text();
                    log.info("TESTO: " + testo.text());
                } else {
                    log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
            }
            //----TOPIC DELL'ARTICOLO----//
            Elements tagClass = doc.getElementsByTag("script");
            if (!tagClass.isEmpty()) {
                List<String> tagList = new ArrayList<>();
                for (Element tag : tagClass) {
                    for (DataNode node : tag.dataNodes()) {
                        if (node.getWholeData().contains("displayTags")) {
                            tagList.addAll(getTagANSA(node.getWholeData()));
                        }
                    }
                }
                for (String t : tagList) {
                    log.info("TAG: " + t);
                }
                article.Topic = tagList;
            } else {
                log.warn("L'applicazione non è in grado di risalire ai tag dell'articolo. " + articleLink);
            }
            if (article.Title != null) {
                MongoDBManager.insertArticle(article);
            }
        } catch (Exception ex) {
            log.warn("URL non valido. " + articleLink);
        }
    }

    private static void scrapeSky(String articleLink) {
        try {
            Article article = new Article();
            article.Newspaper = "SKY TG 24";
            log.info("GIORNALE: " + article.Newspaper);
            Document doc = Jsoup.connect(articleLink).get();
            log.info("LINK: " + articleLink);
            //----TITOLO----//
            Elements titoloClass = doc.getElementsByClass("c-article__title-label");
            if (!titoloClass.isEmpty()) {
                Element titolo = titoloClass.get(0);
                if (titolo != null) {
                    log.info("TITOLO: " + titolo.text());
                    article.Title = titolo.text();
                } else {
                    log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire al titolo dell'articolo. " + articleLink);
            }
            //----TAG----//
            Elements tagClass = doc.getElementsByClass("c-article__category c-news-category");
            if (!tagClass.isEmpty()) {
                Element tag = tagClass.get(0);
                if (tag != null) {
                    List<String> tagList = new ArrayList<>();
                    tagList.add(tag.text());
                    log.info("TAG: " + tag.text());
                    article.Topic = tagList;
                } else {
                    log.warn("L'applicazione non è in grado di risalire ai tag dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire ai tag dell'articolo. " + articleLink);
            }
            //----TESTO DELL'ARTICOLO----//
            Elements testoClass = doc.getElementsByClass("c-article__body j-text-expander");
            if (!testoClass.isEmpty()) {
                Elements pTesto = testoClass.first().getElementsByTag("p");
                String Testo = "";
                for (Element e : pTesto) {
                    Testo += e.text();
                    Testo += " ";
                }
                if (!Testo.equals("")) {
                    log.info("TESTO: " + Testo);
                    article.Text = Testo;
                } else {
                    log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire al testo dell'articolo. " + articleLink);
            }
            //----DATA DEL'ARTICOLO----//
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Elements dataClass = doc.getElementsByClass("c-article__date");
            if (!dataClass.isEmpty()) {
                Element data = dataClass.get(0);
                if (data != null) {
                    try {
                        article.Date = df.parse(getStringDate(data.text()));
                        log.info("DATA: " + article.Date);
                    } catch (ParseException ex) {
                        log.warn("L'applicazione non è in grado di risalire alla data dell'articolo la data è impostata a oggi. " + articleLink);
                        article.Date = new Date();
                        log.info("DATA: " + article.Date);
                    }

                } else {
                    log.warn("L'applicazione non è in grado di risalire alla data dell'articolo la data è impostata a oggi. " + articleLink);
                    article.Date = new Date();
                    log.info("DATA: " + article.Date);
                }
            } else {
                log.warn("L'applicazione non è in grado di risalire alla data dell'articolo la data è impostata a oggi. " + articleLink);
                article.Date = new Date();
                log.info("DATA: " + article.Date);
            }
            if (article.Title != null) {
                MongoDBManager.insertArticle(article);
            }
        } catch (IOException ex) {
            log.warn("URL non valido. " + articleLink);
        }

    }

    private static void writeToFile(List<lastTweetId> ltiList) {
        try (FileOutputStream fos = new FileOutputStream("lastTweetId");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));) {
            for (lastTweetId lti : ltiList) {
                bw.write(lti.toString());
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<lastTweetId> readFromFile() {
        List<lastTweetId> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("lastTweetId");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));) {
            String line = br.readLine();
            while (line != null) {
                String[] splitted = line.split(":");
                result.add(new lastTweetId(splitted[0], Long.parseLong(splitted[1])));
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static String getArticleLink(String Text) {
        String[] splitted = Text.split("\\s+");
        for (String str : splitted) {
            if (str.matches("https\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?")) {
                try {
                    Document t = Jsoup.connect(str).get();
                    String link = t.title();
                    if (link.contains("corriere.it") || link.contains(("sky.tg")) || link.contains("larep.it") || link.contains("csera.it") || link.contains("ansa.it") || link.contains("tg24.sky.it") || link.contains("repubblica.it")) {
                        return link;
                    } else {
                        if (link.contains("ow.ly")) {
                            Document z = Jsoup.connect(link).get();
                            Element l = z.getElementsByTag("link").get(0);
                            return l.attr("href");
                        }
                        return null;
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

    private static Twitter getTwitterFactory() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey("AcfsPSYa9QAU5YFi0hgY7YaW0")
                .setOAuthConsumerSecret("TMnIc1Vs7wuqNsXwnYNEpFdfH6778ZMXkEzKE0bbVe9rdWnci5")
                .setOAuthAccessToken("1209013363600187392-1ljZSN2gWcbw4BS1sC8AYzZCIk1gnI")
                .setOAuthAccessTokenSecret("LLx5Q0zoGYDDHTGSt0SJdm3oysAzyqen5Eiv5DQ1VMELk");

        TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
        twitter4j.Twitter twitter = tf.getInstance();
        return twitter;
    }

    private static lastTweetId findLastTweetId(List<lastTweetId> ltilist, String user) {
        for (lastTweetId lti : ltilist) {
            if (lti.getUser().equals(user)) {
                return lti;
            }
        }
        return null;
    }

    private static boolean allTrueBooleanArray(boolean[] target) {
        for (boolean b : target) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private static List<Status> getStatus(Twitter twitter, String user) {
        Paging paging = new Paging(1, 200);
        List<Status> status = null;
        List<lastTweetId> ltilist = readFromFile();
        Long lastId;
        lastTweetId lti = findLastTweetId(ltilist, user);
        //Account mai scrapato prima
        if (lti == null) {
            ltilist.add(new lastTweetId(user, 1L));
            lastId = 1L;
        } else {
            lastId = findLastTweetId(ltilist, user).getId();
        }

        for (int i = 1; i <= 5; i++) {
            paging.setPage(i);
            paging.sinceId(lastId);
            try {
                if (i == 1) {
                    status = twitter.getUserTimeline(user, paging);
                } else {
                    status.addAll(twitter.getUserTimeline(user, paging));
                }
            } catch (TwitterException ex) {
                java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!status.isEmpty()) {
            findLastTweetId(ltilist, user).setId(status.get(0).getId());
            writeToFile(ltilist);
        } else {
            log.info("L'account @" + user + " non ha postato niente di nuovo dall'ultima volta.");
        }
        return status;
    }

    public static void setAccountToScrape(List<String> account) {
        List<String> result = new ArrayList<>();
        for (String s : account) {
            switch (s) {
                case "La Repubblica":
                    result.add("repubblica");
                    break;
                case "Il Corriere della Sera":
                    result.add("Corriere");
                    break;
                case "ANSA":
                    result.add("Agenzia_Ansa");
                    break;
                case "Sky TG24":
                    result.add("SkyTG24");
                    break;
            }
        }
        accountToScrape = result.toArray(new String[0]);
    }

    public static void scrape() {

        PropertyConfigurator.configure("log4j.properties");
        Twitter twitter = getTwitterFactory();
        List<statusList> statuslist = new ArrayList<>();
        for (String user : accountToScrape) {
            List<Status> status = getStatus(twitter, user);
            if (!status.isEmpty()) {
                statuslist.add(new statusList(user, status));
            }
        }
        int[] indexes = new int[statuslist.size()];
        boolean[] finito = new boolean[statuslist.size()];

        while (!statuslist.isEmpty() && !allTrueBooleanArray(finito)) {
            String articleLink;
            Status currentStatus;
            for (int i = 0; i < statuslist.size(); i++) {
                if (finito[i] == false) {
                    do {
                        log.info("Tweet numero " + indexes[i] + " su " + statuslist.get(i).getStatus().size());
                        currentStatus = statuslist.get(i).getStatus().get(indexes[i]);
                        articleLink = getArticleLink(currentStatus.getText());
                        indexes[i] = indexes[i] + 1;
                    } while (articleLink == null && indexes[i] < statuslist.get(i).getStatus().size());
                    if (indexes[i] == statuslist.get(i).getStatus().size()) {
                        finito[i] = true;
                    } else {
                        switch (statuslist.get(i).getUser()) {
                            case "repubblica":
                                scrapeRepubblica(articleLink);
                                break;
                            case "Corriere":
                                scrapeCorriere(articleLink);
                                break;
                            case "Agenzia_Ansa":
                                scrapeAnsa(articleLink);
                                break;
                            case "SkyTG24":
                                scrapeSky(articleLink);
                        }
                    }
                }
            }
            try {
                Thread.sleep(30 * 1000L);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
