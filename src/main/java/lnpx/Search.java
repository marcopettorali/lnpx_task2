package lnpx;

import java.util.Date;
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
public class Search {

    public String userID;
    public String linkArticle;
    public Date dateRead; //Timestamp?
    public Filters usedFilters;

    public Document toJSON() {
        Document docSearch = new Document();
        docSearch.append("userID", this.userID);
        docSearch.append("linkArticle", this.linkArticle);
        docSearch.append("dateRead", java.time.LocalDate.now());
        Document Filter = usedFilters.toJSON();
        docSearch.append("filters", Filter);
        return docSearch;
    }

}
