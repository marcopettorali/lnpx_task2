package lnpx.messages;

import java.io.Serializable;

public class SignInResponseMsg implements Serializable{

    private int code;

    public SignInResponseMsg(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
