package lnpx.messages;

import java.io.Serializable;
import java.util.*;

public class ChangeSitesMsg implements Serializable{

    private Map<String, Boolean> sites;

    public ChangeSitesMsg(Map<String, Boolean> sites) {
        this.sites = sites;
    }

    public Map<String, Boolean> getSites() {
        return sites;
    }

}
