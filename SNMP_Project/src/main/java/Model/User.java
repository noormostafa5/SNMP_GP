package Model;

import java.util.Date;

public class User {
    private String fName;
    private String lName;
    private int phoneNumber;
    private String nationalID;
    private Date DOB;

    public String getNationalID() {
        return nationalID;
    }

    public void setNationalID(String nationalID) {
        this.nationalID = nationalID;
    }


    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }


    public User(){
        this.fName="empty first name";
        this.lName="empty last name";
        this.phoneNumber=0;
        this.DOB= new Date();
        this.nationalID="0";
    }


    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        String regex="";
        this.lName = lName;
    }


    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        String regex="";
        this.fName = fName;
    }

}
