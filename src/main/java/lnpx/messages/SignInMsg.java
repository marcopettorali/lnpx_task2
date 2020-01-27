package lnpx.messages;

import java.io.*;
import java.util.Date;

public class SignInMsg implements Serializable {

    public String username;
    public String firstName;
    public String lastName;
    public Date dateOfBirth;
    public String email;
    public String password;
    public boolean adminStatus;

    public SignInMsg(String username, String firstName, String lastName, Date dateOfBirth, String email, String password, boolean adminStatus) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.adminStatus = adminStatus;
    }

    public String getUsername() {
        return username;
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

}
