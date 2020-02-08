package lnpx;

import java.util.List;
import twitter4j.Status;

class StatusList {

    private String user;
    private List<Status> status;

    public StatusList(String user, List<Status> status) {
        this.user = user;
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public List<Status> getStatus() {
        return status;
    }

}
