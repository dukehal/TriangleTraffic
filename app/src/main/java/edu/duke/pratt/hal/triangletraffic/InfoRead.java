package edu.duke.pratt.hal.triangletraffic;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
/** This will sort through a text file separated by semi-colons
 *
 * Created by Josh on 3/27/2015.
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

    public ArrayList<VenueInfo> getInfo(InputStreamReader fileReader) throws IOException {
        BufferedReader inputStream = null;

        try {
            inputStream = new BufferedReader(fileReader);
            String l;
            while ((l = inputStream.readLine()) != null) {
                VenueInfo venues = new VenueInfo();
                String[] splitter = l.split(";");
                venues.setName(splitter[0]);
                venues.setLat(Double.parseDouble(splitter[1]));
                venues.setLon(Double.parseDouble(splitter[2]));
                venues.setAddress(splitter[3]);
                venues.setType(splitter[4]);
                venues.setAssoc(splitter[5]);
                venues.setCap(Integer.parseInt(splitter[6]));
                venues.setTrafficLevel(Double.parseDouble(splitter[7]));
                venueList.add(venues);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return venueList;
    }

    public ArrayList<EventInfo> getEventInfo(InputStreamReader fileReader) throws IOException {
        BufferedReader inputStream = null;

        try {
            inputStream = new BufferedReader(fileReader);
            String l;
            while ((l = inputStream.readLine()) != null) {
                EventInfo events = new EventInfo();
                String[] splitter = l.split(";");
                events.setName(splitter[0]);
                events.setTime(splitter[1]);
                events.setDate(splitter[2]);
                eventList.add(events);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return eventList;
    }



}