package lnpx.messages;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrendResponseMsg implements Serializable {

    private LinkedHashMap<String, Long> trendingKeyWords;

    public TrendResponseMsg(LinkedHashMap<String, Long> trendingKeywords) {
        this.trendingKeyWords = trendingKeywords;
    }

    public LinkedHashMap<String, Long> getTrendingKeywords() {
        return trendingKeyWords;
    }
}
