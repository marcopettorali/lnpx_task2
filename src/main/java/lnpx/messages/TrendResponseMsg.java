package lnpx.messages;

import java.io.Serializable;
import java.util.Map;

public class TrendResponseMsg implements Serializable {

    private Map<String, Integer> trendingKeyWords;

    public TrendResponseMsg(Map<String, Integer> trendingKeywords) {
        this.trendingKeyWords = trendingKeywords;
    }

    public Map<String, Integer> getTrendingKeywords() {
        return trendingKeyWords;
    }
}
