package lnpx.messages;

import java.io.Serializable;

public class ChangePeriodMsg implements Serializable{

    private double period;

    public ChangePeriodMsg(double period) {
        this.period = period;
    }

    public double getPeriod() {
        return period;
    }

}
