package edu.duke.pratt.hal.triangletraffic;

public class VenueInfo {

    String name;
    String address;
    double latitude;
    double longitude;
    int capacity;
    String venueType;
    String association;
    double traffic;


    public VenueInfo() {

    }

    public VenueInfo(String n, String a, double lat, double lon, int cap,
                     String type, String assoc, double tl) {
        name = n;
        address = a;
        latitude = lat;
        longitude = lon;
        capacity = cap;
        venueType = type;
        association = assoc;
        traffic = tl;
    }

    public String name() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String address() {
        return address;
    }

    public void setAddress(String a) {
        address = a;
    }

    public double lat() {
        return latitude;
    }

    public void setLat(double lat) {
        latitude = lat;
    }

    public double lon() {
        return longitude;
    }

    public void setLon(double lon) {
        longitude = lon;
    }

    public int cap() {
        return capacity;
    }

    public void setCap(int cap) {
        capacity = cap;
    }

    public String type() {
        return venueType;
    }

    public void setType(String type) {
        venueType = type;
    }

    public String assoc() {
        return association;
    }

    public void setAssoc(String assoc) {
        association = assoc;
    }

    public double trafficLevel() {
        return traffic;
    }

    public void setTrafficLevel(double tl) {
        traffic = tl;
    }
}
