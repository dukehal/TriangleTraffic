package edu.duke.pratt.hal.triangletraffic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;


public class Event extends DatabaseModel {

    // Database Fields
    private int venueId;
    private String name;
    private String description;
    private long unixTimeMillis;
    private boolean tba;
    // Associations
    private Venue venue;


    // Database Model Static and Constructor Definitions (return type cast to relevant subclass)

    public Event(int id) {
        this.map.put(id, this);
        this.setId(id);
    }

    protected static HashMap<Integer, Event> map = new HashMap<>();

    public static Event find(int id) {
        return (Event)Event.map.get(id);
    }

    public static Collection<Event> asCollection() {
        return (Collection<Event>)(Collection<?>)Event.map.values();
    }

    public static ArrayList<Venue> asArrayList() {
        return new ArrayList<Venue>(Venue.asCollection());
    }

    // End Database Model Static Definitions

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUnixTimeMillis() {
        return unixTimeMillis;
    }

    public void setUnixTimeMillis(long unixTimeMillis) {
        this.unixTimeMillis = unixTimeMillis;
    }

    public boolean isTBA() {
        return tba;
    }

    public void setTBA(boolean tba) {
        this.tba = tba;
    }

    public String getDateString() {
        Date utilDate = new Date(this.unixTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d");
        return dateFormat.format(utilDate);
    }

    public void setDate(String date) {
        return;
    }

    public String getTimeString() {
        if (this.isTBA()) {
            return "TBA";
        } else {
            Date utilDate = new Date(this.unixTimeMillis);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma");
            return timeFormat.format(utilDate).replace("AM", "am").replace("PM", "pm");
        }
    }

    public String getTimeUntilString() {
        int dMillis = (int) (this.unixTimeMillis - System.currentTimeMillis());

        int milliseconds_in_second = 1000;
        int milliseconds_in_minute = milliseconds_in_second * 60;
        int milliseconds_in_hour   = milliseconds_in_minute * 60;
        int milliseconds_in_day    = milliseconds_in_hour   * 24;

        int days = dMillis / milliseconds_in_day;
        dMillis -= days * milliseconds_in_day;

        int hours = dMillis / milliseconds_in_hour;
        dMillis -= hours * milliseconds_in_hour;

        int minutes = dMillis / milliseconds_in_minute;
        dMillis -= minutes * milliseconds_in_minute;

        String result = "";

        if (days > 0) {
            result += days + " " + ((days == 1) ? "day" : "days") + " ";
        }

        if (hours > 0) {
            result += hours + " " + ((hours == 1) ? "hour" : "hours") + " ";
        }

        if (minutes > 0) {
            result += minutes + " " + ((minutes == 1) ? "minute" : "minutes") + " ";
        }

        result = result.substring(0, result.length() - 1);

        return result;


    }

    public void setTime(String time) {
        return;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void loadVenueAssociation() {
        this.venue = Venue.find(this.venueId);
    }

}
