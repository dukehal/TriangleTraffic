package edu.duke.pratt.hal.triangletraffic;

/**
 * Created by Josh on 4/21/2015.
 */
public class EventInfo {

    String name;
    String time;
    String date;

    public EventInfo(){}

    public EventInfo(String n, String t, String d) {
        name = n;
        time = t;
        date = d;
    }

    public String name() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String time() {
        return time;
    }

    public void setTime(String t) {
        time = t;
    }

    public String date() {
        return date;
    }

    public void setDate(String d) {
        date = d;
    }



}
