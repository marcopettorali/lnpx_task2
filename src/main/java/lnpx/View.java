package lnpx;

import java.util.Date;
import org.bson.Document;

public class View {

    public String userID;
    public String linkArticle;
    public Date dateRead; //Timestamp?
    public Filters usedFilters;

    public View(String userID, String linkArticle, Date dateRead, Filters usedFilters) {
        this.userID = userID;
        this.linkArticle = linkArticle;
        this.dateRead = dateRead;
        this.usedFilters = usedFilters;
    }

    public Document toJSON() {
        Document docSearch = new Document();
        docSearch.append("userID", this.userID);
        docSearch.append("linkArticle", this.linkArticle);
        docSearch.append("dateRead", dateRead);
        Document Filter = usedFilters.toJSON();
        docSearch.append("filters", Filter);
        return docSearch;
    }

}
