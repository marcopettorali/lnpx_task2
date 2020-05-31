package lnpx;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import java.text.SimpleDateFormat;
import java.util.*;
import org.bson.Document;


public class MongoDBManager {
   private static final MongoClient mongoClient = MongoClients.create("mongodb://"
            + "myUserAdmin:abc123@"
            + "172.16.1.5:27017,"
            + "172.16.1.7:27018,"
            + "172.16.1.8:27019"
            + "/?readPreference=primaryPreferred");
    
    //private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
   
    private static final MongoDatabase database = mongoClient.getDatabase("Article");

    /* ********************* USER Management ******************************** */
    /**
     * This function inserts an user if the userID chosen is available
     * @param u
     * @return 
     */
    public static boolean insertUser(User u) {
        MongoCollection<Document> collection = database.getCollection("Users");
        Document idQuery = new Document("userID", u.userID);
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
        Document andFindQuery=new Document("userID", userId).append("password", password);
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
     * @return  
     */
    public static Map<User, Integer> retrieveUsersInformation() {
        User actualUser;
        ArrayList<String> usersWithSearchs=new ArrayList<>();
        
        Map<User, Integer> usersInfo = new HashMap();
        
        MongoCollection<Document> collection = database.getCollection("Search");
        AggregateIterable<Document> results;
        results = collection.aggregate(Arrays.asList(
                new Document("$group", new Document("_id", "$userID").append("value", new Document("$sum", 1))),
                new Document("$sort", new Document("value", -1))));
        
        //once the aggregation query is executed we need to retrive each user informations
        collection = database.getCollection("Users");
        for (Document dbObject : results) {
            actualUser=new User();
            FindIterable<Document> d = collection.find(new Document("userID", (String) dbObject.get("_id")));
            actualUser.fromJSON(d.first());
            actualUser.setViews((int) dbObject.get("value"));
            usersInfo.put(actualUser, (Integer) dbObject.get("value"));
            usersWithSearchs.add(actualUser.getUserID());
        }
        
        //now we need to restirve the infomarions of the users with 0 searches
        Document usersNotInSearch=new Document("userID",new Document("$nin",usersWithSearchs));
        FindIterable<Document> allUsersNoSearches = collection.find(usersNotInSearch);
        for (Document dbUser : allUsersNoSearches) {
            actualUser= new User();
            actualUser.fromJSON(dbUser);
            actualUser.setViews(0);
            usersInfo.put(actualUser, 0);
        }

        return usersInfo;
    }

    /**
     * This function delete an user and all his history 
     * @param u 
     */
    public static void deleteUser(String userID) {
        MongoCollection<Document> collection = database.getCollection("Search");
        collection.deleteMany(new Document("userID", userID));
        collection = database.getCollection("Users");
        collection.deleteOne(new Document("userID", userID));
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
            u.dateOfBirth=subtractDays(new Date(),(i+j+18)*350);
            u.adminStatus=false;
            insertUser(u);
        }
       }
    }

    /* ******************* END USER Management  ***************************** */
    
    
    /********************* ARTICLE Management ******************************* */
    /**
     * This function is used to insert and Article in the Database
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

    public static void printArticles() {
        MongoCollection<Document> collection = database.getCollection("Article");
        MongoCursor<Document> d = collection.find().iterator();
        System.out.println( d.next().toString());
    }

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
        Document andFindQuery = new Document();
        if(F.keyWord!=null)
            andFindQuery.append("Keywords.keyword", F.keyWord);
        if(F.topic!=null)
            andFindQuery.append("Topic", F.topic);
        if(F.author!=null)
            andFindQuery.append("Authors", F.author); 
        if(F.newspaper!=null)
            andFindQuery.append("Newspaper", F.newspaper);
        //the newspapers we are actually scraping doesn't use country and region
        //we will keep them in case of future widening in choice of newspapers 
        if(F.country!=null)
            andFindQuery.append("Country", F.country);
        if(F.region!=null)
           andFindQuery.append("Region", F.region);
        //
        if(F.city!=null)
            andFindQuery.append("City", F.city);
     
        Date actualDate = new Date();
        Date queryDate = subtractDays(actualDate, 7);
        
        andFindQuery.append("date", new Document( "$gte",queryDate));
 
        //andFindQuery.put("$and", obj);
        
        MongoCursor<Document> cursor = collection.find(andFindQuery).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                Article A = new Article();
                A.fromJSON(d);
                int j;
                for(j=0; j<resultArticles.size();j++)
                {
                    if(resultArticles.get(j).getTitle().compareTo(A.getTitle())==0)
                        break;
                }
                if(j==resultArticles.size())
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

        Date queryDate = subtractDays(actualDate, 7);

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
        Document newUpdate = new Document();
        newUpdate.append("Keywords", keyWordArray);

        Document setQuery = new Document();
        setQuery.append("$set", newUpdate);

        Document queryArticle = new Document();
        queryArticle.append("Title", a.Title);
        
        collection.updateMany(queryArticle, setQuery);
    }

    public static ArrayList<Article> findArticlesNoKeywords() {
        MongoCollection<Document> collection = database.getCollection("Article");
        ArrayList<Article> resultArticles = new ArrayList<>();
        /* *** QUERY *** */
        Document FindQuery = new Document();
        FindQuery.append("Keywords", new Document("$exists", false));

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
    /**
     * This function finds the suggested articles of an user according to the
     * three most used filters
     *
     * @param u needed to retive user information
     * @return suggestedArticles Array of suggested articles for the specific
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
            ArrayList<Article> articles=findArticles(f);
            for(int i=0; i< articles.size();i++)
            {
                int j;
                for(j=0; j<suggestedArticles.size();j++)
                {
                    if(suggestedArticles.get(j).getTitle().compareTo(articles.get(i).getTitle())==0)
                        break;
                }
                if(j==suggestedArticles.size())
                    suggestedArticles.add(articles.get(i));
            }

        }
        return suggestedArticles;

    }

    /* ********************* END STATISTICS User Article ******************** */

    /* ************************ UTILITY functions *************************** */
    /**
     * This function offers an easy way to subtract days from a date
     * @return 
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
        Document obj = new Document();
        obj.append("Topic", 1); //
        obj.append("Date", -1);
        collection.createIndex(obj);
        obj = new Document();
        obj.append("Keywords.keyword", 1);
        obj.append("Date", -1);
        collection.createIndex(obj);
        collection = database.getCollection("Users");
        obj = new Document();
        obj.append("userID", 1);
        collection.createIndex(obj);

    }
    
    public static void closeDB()
    {
        mongoClient.close();
    }
}
