package lnpx;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.bson.Document;

/*
Ricordiamo di chiamare la mongoDB close
 */
public class MongoDBManager {

    private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static final MongoDatabase database = mongoClient.getDatabase("Article");

    /* ********************* USER Management ******************************** */
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

    public static Map<User, Integer> retrieveUsersInformation() {
        User actualUser = new User();
        Map<User, Integer> usersInfo = new HashMap();
        MongoCollection<Document> collection = database.getCollection("Search");
        ArrayList<String> usersAggregation = new ArrayList<>();
        AggregateIterable<Document> results;
        results = collection.aggregate(Arrays.asList(
                new Document("$group", new Document("_id", "$userID").append("value", new Document("$sum", 1))),
                new Document("$sort", new Document("value", -1))));
        collection = database.getCollection("Users");
        FindIterable<Document> allUsers = collection.find();
        for (Document dbUser : allUsers) {
            actualUser.fromJSON(dbUser);
            usersInfo.put(actualUser, 0);
        }
        for (Document dbObject : results) {
            System.out.println("-- - -- --- -- - - - - - - - - - --");
            System.out.println((String) dbObject.get("_id"));
            FindIterable<Document> d = collection.find(new Document("userID", (String) dbObject.get("_id")));
            actualUser.fromJSON(d.first());
            usersInfo.put(actualUser, (Integer) dbObject.get("value"));
        }

        return usersInfo;
    }

    public static void deleteUser(User u) {
        MongoCollection<Document> collection = database.getCollection("Search");
        collection.deleteMany(new Document("userID", u.userID));
        collection = database.getCollection("Users");
        collection.deleteMany(new Document("userID", u.userID));
    }

    /* ******************* END USER Management  ***************************** */
    /**
     * ******************* ARTICLE Management *******************************
     */
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

    //Da aggiungere vincolo della Data
    public static ArrayList<Article> findArticles(Filters F) {
        MongoCollection<Document> collection = database.getCollection("Article");
        ArrayList<Article> resultArticles = new ArrayList<>();
        /* *** QUERY *** */
        BasicDBObject andFindQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<>();
        if(F.keyWord!=null)
            obj.add(new BasicDBObject("Keywords.keyword", F.keyWord));
        if(F.topic!=null)
            obj.add(new BasicDBObject("Topic", F.topic));
        if(F.author!=null)
            obj.add(new BasicDBObject("Authors", F.author)); 
        if(F.newspaper!=null)
            obj.add(new BasicDBObject("Newspaper", F.newspaper));
       /* if(F.country!=null)
            obj.add(new BasicDBObject("Country", F.country));
        if(F.region!=null)
            obj.add(new BasicDBObject("Region", F.region));*/
        if(F.city!=null)
            obj.add(new BasicDBObject("City", F.city));
        andFindQuery.put("$and", obj);

        //System.out.println(andFindQuery.toString());

        MongoCursor<Document> cursor = collection.find(andFindQuery).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                //System.out.println(d.toJson());
                Article A = new Article();
                A.fromJSON(d);
                resultArticles.add(A);
            }
        } finally {
            cursor.close();
        }
        return resultArticles;

    }

    public static LinkedHashMap<String, Long> calculateTrendingKeyWords() {
        Long l=new Long(0);
        Map<String, Long> keyWordValue = new HashMap();
        Integer valueOfKeyword;
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS\'Z\'");

        Date actualDate = new Date();

        Date queryDate = subtractDays(actualDate, 100);

        MongoCollection<Document> collection = database.getCollection("Article");
        // ArrayList<String> trendingKeywords = new ArrayList<>();
        AggregateIterable<Document> results;
        ArrayList<String> indexes=new ArrayList<>();
        indexes.add("$Occur");
        indexes.add("$NumberOfArticles");
        indexes.add("$NumberOfArticles");
        
        results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("date", new Document("$gt", queryDate))),
                new Document("$unwind", "$Keywords"),
                new Document("$group", new Document("_id", "$Keywords.keyword")
                        .append("Occur", new Document("$sum", "$Keywords.Occ"))
                        .append("NumberOfArticles", new Document("$sum", 1))),
                new Document("$project",new Document("_id",1).append("Value", new Document("$multiply",indexes) )),
                new Document("$sort", new Document("Value", -1)),
                new Document("$limit",500)));

        for (Document dbObject : results) {
            //Formula NumeroArticoli^2*Occorenze
            //Integer nOcc = dbObject.getInteger("Occur");
           // Integer nArticle = dbObject.getInteger("NumberOfArticles");
           // valueOfKeyword = nOcc * nArticle * nArticle;
           // keyWordValue.put((String) dbObject.get("_id"), valueOfKeyword);
            if(dbObject.get("Value").getClass()!=l.getClass())
                l=Long.valueOf((Integer) dbObject.get("Value"));
            else
                l=(Long) dbObject.get("Value");
           // Long l=new Long((long) dbObject.get("Value"));
            keyWordValue.put((String) dbObject.get("_id"), l);
            // System.out.println((String) dbObject.get("_id"));
            // trendingKeywords.add((String) dbObject.get("_id"));
        }
        LinkedHashMap<String, Long> reverseSortedMap = new LinkedHashMap<>();
 
        //Use Comparator.reverseOrder() for reverse ordering
        keyWordValue.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 
            .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        System.out.println(reverseSortedMap);
        
        return reverseSortedMap;
    }

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

        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", newUpdate);

        BasicDBObject queryArticle = new BasicDBObject();
        queryArticle.append("Link", a.Link);

        collection.updateOne(queryArticle, setQuery);
    }

    public static ArrayList<Article> findArticlesNoKeywords() {
        MongoCollection<Document> collection = database.getCollection("Article");
        ArrayList<Article> resultArticles = new ArrayList<>();
        /* *** QUERY *** */
        BasicDBObject FindQuery = new BasicDBObject();
        FindQuery.append("Keywords", new BasicDBObject("$exists", false));

        MongoCursor<Document> cursor = collection.find(FindQuery).iterator();
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

    public static void deleteKeywordAnalysis(){
        MongoCollection<Document> collection = database.getCollection("Article");
        collection.updateMany(new Document(),new Document("$unset",new Document("Keywords",1)));
    }
    /* ********************* END ARTICLE Management ************************* */
 /* *********************** VIEW Management ****************************** */
    public static void insertView(View s) {
        MongoCollection<Document> collection = database.getCollection("Search");
        Document docSearch = s.toJSON();
        collection.insertOne(docSearch);
    }

    /* ********************  END VIEW Management **************************** */

 /* ********************** STATISTICS User Article *********************** */
    // forse meglio match concatenato
    /**
     * This function finds the suggested articles of an user according to the
     * three most used filters
     *
     * @param u needed to retive user information
     * @return suggestedArticles Array of suggested articles for the specifi
     * user
     */
    public static ArrayList<Article> suggestedArticles(User u) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS\'Z\'");

        Date actualDate = new Date();

        Date queryDate = subtractDays(actualDate, 7);

        MongoCollection<Document> collection = database.getCollection("Search");
        ArrayList<Article> suggestedArticles = new ArrayList<>();
        AggregateIterable<Document> results;
        //forse le sue match si possono fare con un append!
        results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("userID", u.userID)),
                new Document("$match", new Document("date", new Document("$gt", queryDate))),
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

    /* ********************* END STATISTICS User Article ******************** */

 /* ************************ UTILITY functions *************************** */
    /**
     *
     * @return List<String> This contains the 10 tranding words of the moment
     */
    private static Date subtractDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, -days);

        return cal.getTime();
    }

    /* ********************* INDEXES Management ***************************** */
    public static void createIndexes() {
        MongoCollection<Document> collection = database.getCollection("Article");
        BasicDBObject obj = new BasicDBObject();
        obj.put("Topic", 1); //
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
