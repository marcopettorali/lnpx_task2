package lnpx;

import java.util.Date;
import org.bson.Document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Riccardo
 */
public class User {

    public String userID;
    public String firstName;
    public String lastName;
    public Date dateOfBirth;
    public String email;
    public String password;
    public boolean adminStatus; //true=admin ; false=simple user

    public Document toJSON() {
        Document docUser = new Document();
        docUser.append("userID", this.userID);
        docUser.append("firstName", this.firstName);
        docUser.append("lastName", this.lastName);
        docUser.append("dateOfBirth", this.dateOfBirth);
        docUser.append("email", this.email);
        docUser.append("password", this.password);
        docUser.append("adminStatus", this.adminStatus);
        return docUser;
    }

    public void fromJSON(Document d) {

        this.userID = d.getString("userID");
        this.firstName = d.getString("firstName");
        this.lastName = d.getString("lastName");
        this.dateOfBirth = d.getDate("dateOfBirth");
        this.email = d.getString("email");
        this.password = d.getString("password");
        this.adminStatus=d.getBoolean("adminStatus");
    }

}
