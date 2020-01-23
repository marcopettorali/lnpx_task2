package lnpx.messages;

import java.io.Serializable;

public class ACKMsg implements Serializable{

    private String message;

    public ACKMsg(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
