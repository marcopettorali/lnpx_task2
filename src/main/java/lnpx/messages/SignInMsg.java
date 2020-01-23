package lnpx.messages;

import java.io.*;

public class SignInMsg implements Serializable {

    private String username;
    private String password;
    private String email;
    //[...]

    public SignInMsg(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

}
