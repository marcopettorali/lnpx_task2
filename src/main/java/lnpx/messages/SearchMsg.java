package lnpx.messages;

import java.io.Serializable;

public class SearchMsg implements Serializable{

    private String keyword;
    private String[] filters;
    private String[] values;

    public SearchMsg(String keyword, String[] filters, String[] values) {
        this.keyword = keyword;
        this.filters = filters;
        this.values = values;
    }

    public String getKeyword() {
        return keyword;
    }

    public String[] getFilters() {
        return filters;
    }

    public String[] getValues() {
        return values;
    }

}
