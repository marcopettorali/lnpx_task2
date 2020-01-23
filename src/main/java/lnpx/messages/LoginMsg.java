package lnpx.messages;

import java.io.Serializable;

public class LoginMsg implements Serializable{

    private String username;
    private String password;

    public LoginMsg(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
