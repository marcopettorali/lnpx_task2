package lnpx.messages;

import java.io.Serializable;
import java.util.*;
import lnpx.User;

public class ClientsResponseMsg implements Serializable{

    private List<User> clients;

    public ClientsResponseMsg(List<User> clients) {
        this.clients = clients;
    }

    public List<User> getClients() {
        return clients;
    }

}
