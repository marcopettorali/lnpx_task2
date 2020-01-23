package lnpx;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import java.util.*;
import org.bson.Document;

/*
Ricordiamo di chiamare la mongoDB close
 */
public class MongoDBManager {

    private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static final MongoDatabase database = mongoClient.getDatabase("TryDb");

    public static void insertArticle(Article a) {
        MongoCollection<Document> collection = database.getCollection("Article");
        Document docArticle = a.toJSON();
        collection.insertOne(docArticle);
    }

    public static void printArticles() {
        MongoCollection<Document> collection = database.getCollection("Article");
        FindIterable<Document> d = collection.find();
        System.out.println(collection.countDocuments());
    }

    public static ArrayList<Article> findArticles(Filters F) {
        MongoCollection<Document> collection = database.getCollection("Article");
        ArrayList<Article> resultArticles = new ArrayList<>();
        /* *** QUERY *** */
        BasicDBObject andFindQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<>();
        obj.add(new BasicDBObject("Topic", F.Topic));
        if (!F.Authors.isEmpty()) {
            obj.add(new BasicDBObject("Authors", new BasicDBObject("$all", F.Authors)));  // Dubbio
        }
        obj.add(new BasicDBObject("Newspaper", F.Newspaper));
        obj.add(new BasicDBObject("Country", F.Country));
        obj.add(new BasicDBObject("Region", F.Region));
        obj.add(new BasicDBObject("City", F.City));
        andFindQuery.put("$and", obj);

        System.out.println(andFindQuery.toString());

        MongoCursor<Document> cursor = collection.find(andFindQuery).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                System.out.println(d.toJson());
                Article A = new Article();
                A.fromJSON(d);
                resultArticles.add(A);
            }
        } finally {
            cursor.close();
        }
        return resultArticles;

    }

    public static boolean insertUser(User u) {
        MongoCollection<Document> collection = database.getCollection("Users");
        BasicDBObject idQuery = new BasicDBObject("userID", u.userID);
        if (collection.countDocuments(idQuery) != 0) {
            return false;
        }
        Document docUser = u.toJSON();
        collection.insertOne(docUser);
        return true;
    }

    public static User userAuthentication(String userId, String password) {
        User u = null;
        MongoCollection<Document> collection = database.getCollection("Users");
        BasicDBObject andFindQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<>();
        obj.add(new BasicDBObject("userID", userId));
        obj.add(new BasicDBObject("password", password));
        andFindQuery.put("$and", obj);
        MongoCursor<Document> cursor = collection.find(andFindQuery).iterator();
        try {
            if (cursor.hasNext()) {
                u = new User();
                u.fromJSON(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return u;
    }

    public static void insertSearch(Search s) {
        MongoCollection<Document> collection = database.getCollection("Search");
        Document docSearch = s.toJSON();
        collection.insertOne(docSearch);
    }

    /**
     * This function finds the suggested articles of an user according to the
     * three most used filters
     *
     * @param u needed to retive user information
     * @return suggestedArticles Array of suggested articles for the specifi
     * user
     */
    public static ArrayList<Article> suggestedArticle(User u) {

        MongoCollection<Document> collection = database.getCollection("Search");
        ArrayList<Article> suggestedArticles = new ArrayList<>();
        AggregateIterable<Document> results;
        results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("userID", u.userID)),
                new Document("$group", new Document("_id", "$filters").append("value", new Document("$sum", 1))),
                new Document("$sort", new Document("value", -1)),
                new Document("$limit", 3)));
        for (Document dbObject : results) {
            Document d = (Document) dbObject.get("_id");
            Filters f = new Filters();
            f.fromJSON(d);
            suggestedArticles.addAll(findArticles(f));
            System.out.println(suggestedArticles);

        }
        return suggestedArticles;

    }

    /**
     *
     * @return List<String> This contains the 10 tranding words of the moment
     */
    public static List<String> calculateTrendingKeyWords() {
        MongoCollection<Document> collection = database.getCollection("Article");
        ArrayList<String> trendingKeywords = new ArrayList<>();
        AggregateIterable<Document> results;
        results = collection.aggregate(Arrays.asList(
                new Document("$unwind", "$Keywords"),
                new Document("$group", new Document("_id", "$Keywords.keyword").append("Occur", new Document("$sum", "$Keyword.Occ"))),
                new Document("$sort", new Document("value", -1)),
                new Document("$limit", 10)));
        for (Document dbObject : results) {
            System.out.println((String) dbObject.get("_id"));
            trendingKeywords.add((String) dbObject.get("_id"));
        }
        return trendingKeywords;
    }

    //Manca Le keywords passate e convertite in formato JSON
    public static void insertKeywordAnalysis(Article a, Map<String, Integer> keyWordAnalysis) {
        ArrayList<Document> keyWordArray = new ArrayList<>();
        String[] keys = keyWordAnalysis.keySet().toArray(new String[0]);
        for (int i = 0; i < keyWordAnalysis.size(); i++) {
            Document keyword = new Document();
            keyword.append("keyword", keys[i]);
            keyword.append("Occ", keyWordAnalysis.get(keys[i]));
            keyWordArray.add(keyword);
        }
        MongoCollection<Document> collection = database.getCollection("Article");
        BasicDBObject newUpdate = new BasicDBObject();
        newUpdate.append("Keywords", keyWordArray);

        BasicDBObject queryArticle = new BasicDBObject();
        queryArticle.append("Link", a.Link);

        collection.updateOne(queryArticle, newUpdate);
    }

    public static void createIndexes() {
        MongoCollection<Document> collection = database.getCollection("Article");
        BasicDBObject obj = new BasicDBObject();
        obj.put("Topic", 1);
        obj.put("Date", -1);
        collection.createIndex(obj);
        obj = new BasicDBObject();
        obj.put("Keywords.keyword", 1);
        obj.put("Date", -1);
        collection.createIndex(obj);
        collection = database.getCollection("Users");
        obj = new BasicDBObject();
        obj.put("UserID", 1);
        collection.createIndex(obj);

    }

}
