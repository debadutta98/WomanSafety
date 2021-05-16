package com.debadutta98.womansafty;

public class User {
    String Name;
    String ADDRESS;
    String URL;
    String ID;
    public User(String name, String ADDRESS,String URL,String ID)
    {
        Name = name;
        this.ADDRESS = ADDRESS;
        this.URL=URL;
        this.ID=ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                ", ADDRESS='" + ADDRESS + '\'' +
                ", URL='" + URL + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }
}
