package edu.duke.pratt.hal.triangletraffic;

import java.io.Serializable;

/**
 * Created by Josh on 4/21/2015.
 */
public class EventInfo implements Serializable {

    String name;
    String time;
    String date;
    String event;

    public EventInfo(){}

    public EventInfo(String n, String t, String d, String e) {
        name = n;
        time = t;
        date = d;
        event = e;
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

    public String event() {return event;}

    public void setEvent(String e) {event = e;}



}
