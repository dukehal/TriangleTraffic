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
    private long unixMilliTime;
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

    public long getUnixMilliTime() {
        return unixMilliTime;
    }

    public void setUnixMilliTime(long unixMilliTime) {
        this.unixMilliTime = unixMilliTime;
    }

    public boolean isTBA() {
        return tba;
    }

    public void setTBA(boolean tba) {
        this.tba = tba;
    }

    public String getDateString() {
        Date utilDate = new Date(this.unixMilliTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        return dateFormat.format(utilDate);
    }

    public void setDate(String date) {
        return;
    }

    public String getTimeString() {
        Date utilDate = new Date(this.unixMilliTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma");
        return timeFormat.format(utilDate).replace("AM", "am").replace("PM","pm");
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
