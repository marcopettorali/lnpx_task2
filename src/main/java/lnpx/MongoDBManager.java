package lnpx;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
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
    /**
     * This function inserts an user if the userID Chosen is free
     * @param u
     * @return 
     */
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

    /**
     *  Given an userId and Password this function retive the User information
     * @param userId
     * @param password
     * @return User : null if there is no match
     */
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

    /**
     * This function retive for each user the number of search done, 
     * this function is used by the admins to have an overview of the most active 
     * users
     * @return  Map<User, Integer
     */
    public static Map<User, Integer> retrieveUsersInformation() {
        User actualUser = new User();
        Map<User, Integer> usersInfo = new HashMap();
        
        MongoCollection<Document> collection = database.getCollection("Search");
        AggregateIterable<Document> results;
        results = collection.aggregate(Arrays.asList(
                new Document("$group", new Document("_id", "$userID").append("value", new Document("$sum", 1))),
                new Document("$sort", new Document("value", -1))));
        
        collection = database.getCollection("Users");
        FindIterable<Document> allUsers = collection.find();
        for (Document dbUser : allUsers) {
            actualUser.fromJSON(dbUser);
             actualUser.setViews(0);
            usersInfo.put(actualUser, 0);
        }
        
        for (Document dbObject : results) {
            FindIterable<Document> d = collection.find(new Document("userID", (String) dbObject.get("_id")));
            actualUser.fromJSON(d.first());
            actualUser.setViews((int) dbObject.get("value"));
            usersInfo.put(actualUser, (Integer) dbObject.get("value"));
        }

        return usersInfo;
    }

    /**
     * This function delete an user and all his history 
     * @param u 
     */
    public static void deleteUser(User u) {
        MongoCollection<Document> collection = database.getCollection("Search");
        collection.deleteMany(new Document("userID", u.userID));
        collection = database.getCollection("Users");
        collection.deleteMany(new Document("userID", u.userID));
    }
    /**
     * This function is used to fill the User collection for indexes statistics
     */
    public static void populateUsers()
    {
        User u=new User();
        String[] FName={"Giuseppe","Maria","Giovanni","Anna","Antonio","Giuseppina","Mario","Rosa","Luigi","Angela","Francesco","Giovanna","Angelo","Teresa","Vincenzo","Lucia","Pietro","Carmela","Salvatore","Caterina","Carlo","Francesca","Franco","Anna Maria","Domenico","Antonietta","Bruno","Carla","Paolo","Elena","Michele","Concetta","Giorgio","Rita","Aldo","Margherita","Sergio","Franca","Luciano","Paola"};
        String[] LName={"Rossi","Russo","Ferrari","Esposito","Bianchi","Romano","Colombo","Ricci","Marino","Greco","Bruno","Gallo","Conti","De Luca","Mancini","Costa","Giordano","Rizzo","Lombardi","Moretti","Barbieri","Fontana","Santoro","Mariani","Rinaldi","Caruso","Ferrara","Galli","Martini","Leone","Longo","Gentile","Martinelli","Vitale","Lombardo","Serra","Coppola","De Santis","D'angelo","Marchetti"};
       for(int j=0;j<LName.length;j++)
       {
        for(int i=0;i<FName.length;i++)
        {
            u.userID=FName[i]+LName[j];
            u.firstName=FName[i];
            u.lastName=LName[j];
            u.email=FName[i]+LName[j]+"@gmail.com";
            u.password=FName[i]+LName[j];
            u.adminStatus=false;
            insertUser(u);
        }
       }
    }

    /* ******************* END USER Management  ***************************** */
    
    
    /********************* ARTICLE Management ******************************* */
    /**
     * This function s used to insert and Article in the Database
     * @param a 
     */
    public static void insertArticle(Article a) {
        if(a.getText() == null){
            return;
        }
        MongoCollection<Document> collection = database.getCollection("Article");
        Document docArticle = a.toJSON();
        collection.insertOne(docArticle);
    }

    /*public static void printArticles() {
        MongoCollection<Document> collection = database.getCollection("Article");
        FindIterable<Document> d = collection.find();
        System.out.println(collection.countDocuments());
    }*/

    /**
     * This function retrives the articles that match the chosen Filters
     * @param F
     * @return 
     */
    public static ArrayList<Article> findArticles(Filters F) {
        MongoCollection<Document> collection = database.getCollection("Article");
        
        /*SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS\'Z\'");*/
        
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
        //the newspapers we are acutaly scapring doesn't use country and region
        //we will keep them in case of future widening in choice of newspapers 
        if(F.country!=null)
            obj.add(new BasicDBObject("Country", F.country));
        if(F.region!=null)
            obj.add(new BasicDBObject("Region", F.region));
        //
        if(F.city!=null)
            obj.add(new BasicDBObject("City", F.city));
     
        Date actualDate = new Date();
        Date queryDate = subtractDays(actualDate, 20);
        
        obj.add(new BasicDBObject("date", BasicDBObjectBuilder.start( "$gte",queryDate).get()));
 
        andFindQuery.put("$and", obj);
        
        MongoCursor<Document> cursor = collection.find(andFindQuery).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
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
        Long valueOfKeyword=new Long(0);
        Map<String, Long> keyWordValue = new HashMap();
        
        Date actualDate = new Date();

        Date queryDate = subtractDays(actualDate, 100);

        MongoCollection<Document> collection = database.getCollection("Article");
        
        ArrayList<String> indexes=new ArrayList<>();
        indexes.add("$Occur");
        indexes.add("$NumberOfArticles");
        indexes.add("$NumberOfArticles");
        
        AggregateIterable<Document> results;
        //ValueOfKeyword=NumberOfArticles^2*Occ (Explained in the Documentation)
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
            if(dbObject.get("Value").getClass()!=valueOfKeyword.getClass())
                valueOfKeyword=Long.valueOf((Integer) dbObject.get("Value"));
            else
                valueOfKeyword=(Long) dbObject.get("Value");
            keyWordValue.put((String) dbObject.get("_id"), valueOfKeyword);
        }
        
        //Ordered Map by Value
        LinkedHashMap<String, Long> reverseSortedMap = new LinkedHashMap<>();
        keyWordValue.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 
            .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        
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
        queryArticle.append("Title", a.Title);
        
        collection.updateMany(queryArticle, setQuery);
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
    
    public static void deleteArticleNoKeywords(){
         MongoCollection<Document> collection = database.getCollection("Article");
         collection.deleteMany(new Document("Keywords",new Document("$exists",false)));
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
                Aggregates.match(and(eq("userID",u.userID),gte("dateRead",queryDate))),
                new Document("$group", new Document("_id", "$filters").append("value", new Document("$sum", 1))),
                new Document("$sort", new Document("value", -1)),
                new Document("$limit", 3)));
        for (Document dbObject : results) {
            Document d = (Document) dbObject.get("_id");
            Filters f = new Filters();
            System.out.println(d);
            f.fromJSON(d);
            suggestedArticles.addAll(findArticles(f));

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
        obj.put("userID", 1);
        collection.createIndex(obj);

    }
    
    public static void closeDB()
    {
        mongoClient.close();
    }
}
