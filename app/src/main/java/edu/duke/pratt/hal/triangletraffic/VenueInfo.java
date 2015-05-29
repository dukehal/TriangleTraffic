package edu.duke.pratt.hal.triangletraffic;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class VenueInfo implements Serializable {

    String name;
    String address;
    double latitude;
    double longitude;
    int capacity;
    String venueType;
    String association;
    double traffic;
    ArrayList<EventInfo> eventList;


    public VenueInfo() {

    }

    public VenueInfo(String n, String a, double lat, double lon, int cap,
                     String type, String assoc, double tl, ArrayList<EventInfo> eInfo) {
        name = n;
        address = a;
        latitude = lat;
        longitude = lon;
        capacity = cap;
        venueType = type;
        association = assoc;
        traffic = tl;
        eventList = eInfo;
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

    public ArrayList<EventInfo> eInfo() {
        return eventList;
    }

    public void setEvents(ArrayList<EventInfo> eInfo) {
        eventList = eInfo;
    }


    public static ArrayList<VenueInfo> getVenueInfo(Context context) {
        // Defined Array values to show in ListView
        InfoRead info = new InfoRead();
        InputStream inputStream = null;
        InputStream eventInputStream = null;

        AssetManager assetManager = context.getAssets();

        ArrayList<VenueInfo> venues = new ArrayList<>();


        try {

            inputStream = assetManager.open("venues_information.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            eventInputStream = assetManager.open("events_information.txt");
            InputStreamReader eventInputStreamReader = new InputStreamReader(eventInputStream);

            String l;
            BufferedReader bufferedVenueReader = new BufferedReader(inputStreamReader);
            BufferedReader bufferedEventReader = new BufferedReader(eventInputStreamReader);


            while ((l = bufferedVenueReader.readLine()) != null) {
                VenueInfo venue = new VenueInfo();
                String[] splitter = l.split(";");
                venue.setName(splitter[0]);
                venue.setLat(Double.parseDouble(splitter[1]));
                venue.setLon(Double.parseDouble(splitter[2]));
                venue.setAddress(splitter[3]);
                venue.setType(splitter[4]);
                venue.setAssoc(splitter[5]);
                venue.setCap(Integer.parseInt(splitter[6]));
                venue.setTrafficLevel(Double.parseDouble(splitter[7]));





                venues.add(venue);
            }



            while ((l = bufferedEventReader.readLine()) != null) {
                EventInfo event = new EventInfo();
                String[] splitter = l.split(";");
                event.setVenue(splitter[0]);
                event.setEvent(splitter[1]);
                event.setDate(splitter[2]);
                event.setTime(splitter[3]);
                for(int i = 0; i<venues.size(); i++) {
                    if (splitter[0] == venues.get(i).name()) {
                        venues.get(i).eventList.add(event);
                    }
                }
            }


            return venues;



        } catch(IOException ie) {
            ie.printStackTrace();
        }

        return venues;

    }

}
