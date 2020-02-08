package lnpx;

import org.bson.Document;

public class Filters {

    public String keyWord;
    public String topic;
    public String author;
    public String newspaper;
    public String country;
    public String region;
    public String city;

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
        if (this.keyWord != null) {
            docFilters.append("Keyword", this.keyWord);
        }
        if (this.topic != null) {
            docFilters.append("Topic", this.topic);
        }
        if (this.author != null) {
            docFilters.append("Authors", this.author);
        }
        if (this.newspaper != null) {
            docFilters.append("Newspaper", this.newspaper);
        }
        if (this.country != null) {
            docFilters.append("Country", this.country);
        }
        if (this.region != null) {
            docFilters.append("Region", this.region);
        }
        if (this.city != null) {
            docFilters.append("City", this.city);
        }

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
