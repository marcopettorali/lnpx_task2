package lnpx.messages;

import java.io.Serializable;

public class TrendResponseMsg implements Serializable {

    private String topics[];
    private double values[];

    public TrendResponseMsg(String[] topics, double[] values) {
        this.topics = topics;
        this.values = values;
    }

    public String[] getTopics() {
        return topics;
    }

    public double[] getValues() {
        return values;
    }
}
