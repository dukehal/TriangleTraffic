package edu.duke.pratt.hal.triangletraffic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class Venue extends DatabaseModel {

    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int capacity;
    private String venueType;
    private String association;
    private double traffic;
    private ArrayList<Event> events;

    // Database Model Static and Constructor Definitions (return type cast to relevant subclass)

    protected static HashMap<Integer, Venue> map = new HashMap<>();

    public Venue(int id) {
        this.map.put(id, this);
        this.setId(id);
    }

    public static Venue find(int id) {
        return (Venue)Venue.map.get(id);
    }

    public static Collection<Venue> asCollection() {
        return (Collection<Venue>)(Collection<?>)Venue.map.values();
    }

    public static ArrayList<Venue> asArrayList() {
        return new ArrayList<Venue>(Venue.asCollection());
    }

    // End Database Model Static Definitions

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public double getTraffic() {
        return traffic;
    }

    public void setTraffic(double traffic) {
        this.traffic = traffic;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public void loadEventsAssociation() {
        this.events = new ArrayList<>();
        for (Event event : Event.asCollection()) {
            if (event.getVenueId() == this.getId()) {
                this.events.add(event);
            }
        }
    }


}
