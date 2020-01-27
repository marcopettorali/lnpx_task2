package lnpx.messages;

import java.util.*;

public class ViewMsg {
    private String linkArticle;
    private Map<String, String> filters;

    public ViewMsg(String linkArticle, Map<String, String> filters) {
        this.linkArticle = linkArticle;
        this.filters = filters;
    }

    public String getLinkArticle() {
        return linkArticle;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
    
    
    
}
