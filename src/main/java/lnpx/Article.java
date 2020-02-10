package lnpx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class Article implements Serializable{

    public String Title;            //mandatory
    public String Link;             //mandatory
    public List<String> Topic;            //mandatory
    public List<String> Authors;  //mandatory One or More
    public String Newspaper;        //mandatory
    public String Text;             //mandatory 
    public Date Date;             //mandatory
    public String Country;
    public String Region;
    public String City;
    public Map<String, Integer> keyWordAnalysis; //pensa a come trasformare la keyword doc in un Map
    
    public String getTitle() {
        return Title;
    }

    public String getLink() {
        return Link;
    }

    public List<String> getTopic() {
        return Topic;
    }

    public List<String> getAuthors() {
        return Authors;
    }

    public String getNewspaper() {
        return Newspaper;
    }

    public String getText() {
        return Text;
    }

    public Date getDate() {
        return Date;
    }

    public String getCountry() {
        return Country;
    }

    public String getRegion() {
        return Region;
    }

    public String getCity() {
        return City;
    }

    public Map<String, Integer> getKeyWordAnalysis() {
        return keyWordAnalysis;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public void setLink(String Link) {
        this.Link = Link;
    }

    public void setTopic(List<String> Topic) {
        this.Topic = Topic;
    }

    public void setAuthors(List<String> Authors) {
        this.Authors = Authors;
    }

    public void setNewspaper(String Newspaper) {
        this.Newspaper = Newspaper;
    }

    public void setText(String Text) {
        this.Text = Text;
    }

    public void setDate(Date Date) {
        this.Date = Date;
    }

    public void setCountry(String Country) {
        this.Country = Country;
    }

    public void setRegion(String Region) {
        this.Region = Region;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public void setKeyWordAnalysis(Map<String, Integer> keyWordAnalysis) {
        this.keyWordAnalysis = keyWordAnalysis;
    }
   
    
    public Document toJSON() {
        Document docArticle = new Document("Title", this.Title).append("Link", this.Link).append("Topic", this.Topic).append("Newspaper", this.Newspaper).append("Text", this.Text).append("date", this.Date);
        if (this.Authors != null) {
            docArticle.append("Authors", this.Authors);
        }
        if (this.Country != null) {
            docArticle.append("Country", this.Country);
        }
        if (this.Region != null) {
            docArticle.append("Region", this.Region);
        }
        if (this.City != null) {
            docArticle.append("City", this.City);
        }

        return docArticle;
    }

    public void fromJSON(Document d) {
        keyWordAnalysis=new HashMap<>();
        this.Title = (String) d.get("Title");
        this.Link = (String) d.get("Link");
        this.Topic = (List<String>) d.get("Topic");
        this.Authors = (List<String>) d.get("Authors");
        this.Newspaper = (String) d.get("Newspaper");
        this.Text = (String) d.get("Text");
        this.Date = (Date) d.get("Date");
        this.Country = (String) d.get("Country");
        this.Region = (String) d.get("Region");
        this.City = (String) d.get("City");
        if(d.get("Keywords")!=null)
        {
            ArrayList<org.bson.Document> keywords=(ArrayList<org.bson.Document>) d.get("Keywords");
            for (Document k : keywords) {
                keyWordAnalysis.put((String) k.get("keyword"), (Integer) k.get("Occ"));
            }
        }
    }
}
