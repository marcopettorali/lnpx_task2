package lnpx.messages;

import java.io.Serializable;
import java.util.*;

public class FindMsg implements Serializable{

    private String keyword;
    private Map<String, String> filters;

    public FindMsg(String keyword, Map<String, String> filters) {
        this.keyword = keyword;
        this.filters = filters;
    }

    public String getKeyword() {
        return keyword;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
    
}
