package lnpx.messages;

import java.io.Serializable;

public class LoginResponseMsg implements Serializable{

    private int code;

    public LoginResponseMsg(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
