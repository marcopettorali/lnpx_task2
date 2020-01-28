package lnpx;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Riccardo
 */
public class Article {

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
    public  Map<String, Integer> keyWordAnalysis; //pensa a come trasformare la keyword doc in un Map
    
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
    }
}
