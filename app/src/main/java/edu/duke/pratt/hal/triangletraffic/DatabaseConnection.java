package edu.duke.pratt.hal.triangletraffic;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DatabaseConnection {


    public DatabaseConnection(Context context) {

        String venues_information_file = "venues_information.txt";
        String events_information_file = "venues_information.txt";
        AssetManager am = context.getAssets();

        try {
            InputStream venuesInputStream = am.open(venues_information_file);
            InputStream eventsInputStream = am.open(events_information_file);

            BufferedReader venuesLineByLine = new BufferedReader(new InputStreamReader(venuesInputStream));
            BufferedReader eventsLineByLine = new BufferedReader(new InputStreamReader(eventsInputStream));

            String line;

            // Load Venues.
            while ((line = venuesLineByLine.readLine()) != null) {
                generateVenue(line);
            }

            // Load Events.
            while ((line = eventsLineByLine.readLine()) != null) {
                generateEvent(line);
            }

            // Connect Events to their associated Venue.
            for ( Event event : Event.asCollection() ) {
                event.loadVenueAssociation();
            }

            // Connect Venues to their associated Events.
            for ( Venue venue : Venue.asCollection() ) {
                venue.loadEventsAssociation();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Venue generateVenue(String databaseRow) {

        String[] data = databaseRow.split(";");

        Venue venue = new Venue(Integer.parseInt(data[0]));
        venue.setName(data[1]);
        venue.setLatitude(Double.parseDouble(data[2]));
        venue.setLongitude(Double.parseDouble(data[3]));
        venue.setAddress(data[4]);
        venue.setVenueType(data[5]);
        venue.setAssociation(data[6]);
        venue.setCapacity(Integer.parseInt(data[7]));
        venue.setTraffic(Double.parseDouble(data[8]));

        return venue;

    }

    private Event generateEvent(String databaseRow) {

        String[] data = databaseRow.split(";");


        Event event = new Event(Integer.parseInt(data[0]));
        event.setName(data[2]);
        event.setDate(data[3]);
        event.setTime(data[4]);

        return event;

    }


}
