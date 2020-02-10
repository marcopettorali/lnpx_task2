package lnpx;

import org.bson.Document;

public class Filters {

    public String keyWord = null;
    public String topic = null;
    public String author = null;
    public String newspaper = null;
    public String country = null;
    public String region = null;
    public String city = null;

    public Filters() {
    }

    public Filters(String Keyword, String Topic, String Author, String Newspaper, String Country, String Region, String City) {

        this.keyWord = Keyword;

        this.topic = Topic;

        this.author = Author;

        this.newspaper = Newspaper;

        this.country = Country;

        this.region = Region;

        this.city = City;
    }

    public Document toJSON() {
        Document docFilters = new Document();

        docFilters.append("Keyword", this.keyWord);

        docFilters.append("Topic", this.topic);

        docFilters.append("Authors", this.author);

        docFilters.append("Newspaper", this.newspaper);

        docFilters.append("Country", this.country);

        docFilters.append("Region", this.region);

        docFilters.append("City", this.city);

        return docFilters;
    }

    public void fromJSON(Document d) {
        this.keyWord = (String) d.get("Keyword");
        this.topic = (String) d.get("Topic");
        this.author = (String) d.get("Authors");
        this.newspaper = (String) d.get("Newspaper");
        this.country = (String) d.get("Country");
        this.region = (String) d.get("Region");
        this.city = (String) d.get("City");
    }

}
