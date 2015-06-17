package edu.duke.pratt.hal.triangletraffic.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import edu.duke.pratt.hal.triangletraffic.utility.Distance;


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

    public ArrayList<Event> getPresentEvents() {
        ArrayList<Event> presentEvents = new ArrayList<>();
        for (Event event : this.events ) {
            if (event.getUnixTimeMillis() > System.currentTimeMillis()) {
                presentEvents.add(event);
            }
        }
        return presentEvents;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    // On resume, oncreate, will read notification.
    // On pause, ondestroy will save notification.

    public boolean getNotification() {
//        if (notifications != null) {
//            return notifications;
//        } else {
//            return true;
//        }
//        return notifications;
        return true;
    }

    // Sets the field, without saving preference to field.
    public void setNotification(boolean notification) {
        this.notifications = notifications;
    }

    public void loadSettings(Context context) {

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

    public void saveSettings(Context context, ArrayList<Venue> venueArrayList) {
        String venue_notifications_file = "venue_notifications.txt";
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(venue_notifications_file, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            // Find the line where settings for this venue is stored.
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

    public void loadEventsAssociation() {
        this.events = new ArrayList<>();
        for (Event event : Event.asCollection()) {
            if (event.getVenueId() == this.getId()) {
                this.events.add(event);
            }
        }
    }

    public Distance distanceFrom(Location currentLocation) {

        float[] results = new float[2];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), this.getLatitude(), this.getLongitude(), results);

        return new Distance(results[0]);

    }

    public Event nextEvent() {
        ArrayList<Event> presentEvents = this.getPresentEvents();
        Collections.sort(presentEvents);
        if (presentEvents.size() > 0) {
            return presentEvents.get(0);
        } else {
            return null;
        }
    }

    public static ArrayList<Venue> sortedVenuesByDistanceTo (final Location location) {
        ArrayList<Venue> result = new ArrayList<>(Venue.asArrayList());

        class VenueByLocationComparator implements Comparator<Venue> {
            @Override
            public int compare(Venue lhs, Venue rhs) {
                double distance1 = lhs.distanceFrom(location).getMeters();
                double distance2 = rhs.distanceFrom(location).getMeters();
                return (int) (distance1 - distance2);
            }
        }

        Collections.sort(result, new VenueByLocationComparator());
        return result;

    }

    public LatLng getLatLng() {
        return new LatLng(this.getLatitude(), this.getLongitude());
    }
}
