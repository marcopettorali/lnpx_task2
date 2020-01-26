package lnpx;

import java.util.Date;
import java.util.List;
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
public class Filters {

    public String Keyword;
    public String Topic;
    public List<String> Authors;
    public String Newspaper;
    public String Country;
    public String Region;
    public String City;

    public Document toJSON() {
        Document docFilters = new Document();
        if (this.Keyword!=null){
              docFilters.append("Keyword", this.Keyword);
        }
        if (this.Topic != null) {
            docFilters.append("Topic", this.Topic);
        }
        if (this.Authors != null) {
            docFilters.append("Authors", this.Authors);
        }
        if (this.Newspaper != null) {
            docFilters.append("Newspaper", this.Newspaper);
        }
        if (this.Country != null) {
            docFilters.append("Country", this.Country);
        }
        if (this.Region != null) {
            docFilters.append("Region", this.Region);
        }
        if (this.City != null) {
            docFilters.append("City", this.City);
        }

        return docFilters;
    }

    public void fromJSON(Document d) {
        this.Keyword = (String) d.get("Keyword");
        this.Topic = (String) d.get("Topic");
        this.Authors = (List<String>) d.get("Authors");
        this.Newspaper = (String) d.get("Newspaper");
        this.Country = (String) d.get("Country");
        this.Region = (String) d.get("Region");
        this.City = (String) d.get("City");
    }

}
