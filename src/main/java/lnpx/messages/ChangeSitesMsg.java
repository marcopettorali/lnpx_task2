package lnpx.messages;

import java.io.Serializable;

public class ChangeSitesMsg implements Serializable{

    private String[] sites;
    private boolean[] values;

    public ChangeSitesMsg(String[] sites, boolean[] values) {
        this.sites = sites;
        this.values = values;
    }

    public String[] getSites() {
        return sites;
    }

    public boolean[] getValues() {
        return values;
    }

}
