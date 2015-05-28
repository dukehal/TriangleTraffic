package edu.duke.pratt.hal.triangletraffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
/** This will sort through a text file separated by semi-colons
 *
 * Created by Josh on 3/27/2015.
 */
import java.io.InputStreamReader;

public class InfoRead {
    ArrayList<VenueInfo> venueList = new ArrayList<>();
    ArrayList<EventInfo> eventList = new ArrayList<>();

    public InfoRead(){}

    public InfoRead(ArrayList<VenueInfo> v, ArrayList<EventInfo> e){
        for(int i = 0; i<v.size(); i++) {
            venueList.add(v.get(i));
        }

        for(int i = 0; i<v.size(); i++) {
            eventList.add(e.get(i));
        }
    }

    public ArrayList<VenueInfo> getVenueInfo(InputStreamReader fileReader, InputStreamReader eventFileReader) throws IOException {
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(fileReader);
            String l;
            while ((l = inputStream.readLine()) != null) {
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
                venueList.add(venue);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        BufferedReader eventInputStream = null;

        try {
            eventInputStream = new BufferedReader(eventFileReader);
            String l;
            while ((l = eventInputStream.readLine()) != null) {
                EventInfo event = new EventInfo();
                String[] splitter = l.split(";");
                event.setVenue(splitter[0]);
                event.setEvent(splitter[1]);
                event.setDate(splitter[2]);
                event.setTime(splitter[3]);
                for(int i = 0; i<venueList.size(); i++) {
                    if (splitter[0] == venueList.get(i).name()) {
                        venueList.get(i).eventList.add(event);
                    }
                }
            }
        } finally {
            if (eventInputStream != null) {
                eventInputStream.close();
            }
        }
        return venueList;
    }
}