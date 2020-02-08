package lnpx;

import java.util.List;
import twitter4j.Status;

class statusList {

    private String user;
    private List<Status> status;

    public statusList(String user, List<Status> status) {
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
