package edu.duke.pratt.hal.triangletraffic;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    private boolean notifications;

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

    public boolean getNotification() {
        return notifications;
    }

    public void setNotification(boolean notification) {
        this.notifications = notifications;
    }

    public void save(Context context, ArrayList<Venue> venueArrayList) {
        String venue_notifications_file = "venue_notifications.txt";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(venue_notifications_file, Context.MODE_PRIVATE));
            for(int i = 0; i < venueArrayList.size(); i++) {
                if(venueArrayList.get(i).getNotification() == true) {
                    outputStreamWriter.write("true");
                } else {
                    outputStreamWriter.write("false");
                }
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.w("Exception", "File write failed: " + e.toString());
        }
    }

    public void readNotifications(Context context) {

        String venue_notifications_file = "venue_notifications.txt";

        try {
            InputStream inputStream = context.openFileInput(venue_notifications_file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                String ret = stringBuilder.toString();
                Log.w("it's working!", ret);
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
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
