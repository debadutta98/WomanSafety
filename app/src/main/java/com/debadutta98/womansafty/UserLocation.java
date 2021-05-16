package com.debadutta98.womansafty;

public class UserLocation {
    String  LAT;
    String LOG;
    String NAME;
    String URL;
String ID;
    public UserLocation(String LAT, String LOG, String NAME, String URL, String ID) {
        this.LAT = LAT;
        this.LOG = LOG;
        this.NAME = NAME;
        this.URL = URL;
        this.ID=ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLAT() {
        return LAT;
    }

    public void setLAT(String LAT) {
        this.LAT = LAT;
    }

    public String getLOG() {
        return LOG;
    }

    public void setLOG(String LOG) {
        this.LOG = LOG;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
