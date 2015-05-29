package edu.duke.pratt.hal.triangletraffic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class Event extends DatabaseModel {

    // Database Fields
    private int venueId;
    private String name;
    private String description;
    private String date;
    private String time;

    // Associations
    private Venue venue;


    // Database Model Static and Constructor Definitions (return type cast to relevant subclass)

    public Event(int id) {
        this.map.put(id, this);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
