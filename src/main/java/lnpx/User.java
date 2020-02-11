package lnpx;

import java.io.Serializable;
import java.util.Date;
import org.bson.Document;

public class User implements Serializable{

    public String userID;
    public String firstName;
    public String lastName;
    public Date dateOfBirth;
    public String email;
    public String password;
    public boolean adminStatus; //true=admin ; false=simple user

    public User() {
    }

    public User(String userID, String firstName, String lastName, Date dateOfBirth, String email, String password, boolean adminStatus) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.adminStatus = adminStatus;
    }

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
        this.adminStatus = d.getBoolean("adminStatus");
    }
    
      public String getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdminStatus() {
        return adminStatus;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAdminStatus(boolean adminStatus) {
        this.adminStatus = adminStatus;
    }
    

}
