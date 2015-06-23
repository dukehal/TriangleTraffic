package edu.duke.pratt.hal.triangletraffic.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;

import edu.duke.pratt.hal.triangletraffic.PreferenceConnection;
import edu.duke.pratt.hal.triangletraffic.R;
import edu.duke.pratt.hal.triangletraffic.model.Event;
import edu.duke.pratt.hal.triangletraffic.model.NotificationInfo;
import edu.duke.pratt.hal.triangletraffic.model.Venue;
import edu.duke.pratt.hal.triangletraffic.utility.AppPref;
import edu.duke.pratt.hal.triangletraffic.utility.DatabaseConnection;
import edu.duke.pratt.hal.triangletraffic.utility.Distance;

public class NotificationService extends Service implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    GoogleApiClient client;
    HashMap<Event, Long> eventsNotified = new HashMap<>();
    ArrayList<Venue> venues;
    Location currentLocation;
    private LocationRequest locationRequest;
    private long lastRecordedTime;
    private Location lastRecordedLocation;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        new DatabaseConnection(this);
        venues = Venue.asArrayList();

        new PreferenceConnection(this);
        buildGoogleApiClient();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastRecordedLocation == null) {
            lastRecordedLocation = location;
        }

        Log.w("current lat", Double.toString(currentLocation.getLatitude()));
        Log.w("current long", Double.toString(currentLocation.getLongitude()));

        NotificationInfo notificationToSend = shouldSendNotification();
        Event event;

        if(lastRecordedLocation == null) {
            lastRecordedLocation = currentLocation;
            lastRecordedTime = System.currentTimeMillis();
        } else {
            //shouldSendNotification();
            if (notificationToSend != null) {
                event = notificationToSend.getEvent();
                String notificationText = event.getName() + " has an event today at "
                        + event.getTimeString();
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext());

                Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + getPackageName() + "/raw/notification");
//                if (alarmSound == null) {
//                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                }

                if (AppPref.textMode()) {
                    mBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setSmallIcon(R.drawable.basketball_ball)
                            .setContentTitle("Triangle Traffic")
                            .setContentText(notificationText)
                            .setTicker(notificationText);
                }

                if (AppPref.audioMode()) {
//                    mBuilder.setSound(alarmSound);
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }

                if (AppPref.vibrateMode()) {
                    mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                }

                int mNotificationId = event.getVenueId();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    public NotificationInfo shouldSendNotification() {

        long eventTime;
        long notificationTime;
        long currentTime = System.currentTimeMillis();
        Double notificationRadius = AppPref.getRadiusMeters();


        float[] results = new float[2];
        Event eventNotified = null;

        // Make local copies of last recorded time and location.
        Long lastRecordedTimeLocalCopy = lastRecordedTime;
        Location lastRecordedLocationLocalCopy = lastRecordedLocation;

        // Update global last recorded time and location before we enter for each loops.
        lastRecordedTime = currentTime;
        lastRecordedLocation = currentLocation;

        for (Venue venue : venues) {

            if (venue.getNotification()) {

                ArrayList<Event> venueEvents = venue.getPresentEvents();

                Location.distanceBetween(
                        lastRecordedLocationLocalCopy.getLatitude(),
                        lastRecordedLocationLocalCopy.getLongitude(),
                        venue.getLatitude(),
                        venue.getLongitude(),
                        results);

                Distance lastRecordedDistance = new Distance(results[0]);

                Location.distanceBetween(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        venue.getLatitude(),
                        venue.getLongitude(),
                        results);

                Distance currentDistance = new Distance(results[0]);

                boolean distanceCrossing =
                        (lastRecordedDistance.getMeters() >= notificationRadius &&
                                currentDistance.getMeters() <= notificationRadius);

                boolean distanceCrossed =
                        (lastRecordedDistance.getMeters() <= notificationRadius &&
                                currentDistance.getMeters() <= notificationRadius);

                if (venue.getId()==2) {
//                    dlog("Wallace distanceCrossing: " + distanceCrossing + ",     Wallace distanceCrossed: " + distanceCrossed);
                    String lastVsCurr = String.format("lastRecordedDistance: %.2f m,     currentDistance: %.2f m",
                            lastRecordedDistance.getMeters(), currentDistance.getMeters());
//                    dlog(lastVsCurr);
//                    dlog("notificationRadius: " + notificationRadius + " m");
                }


                for (Event event : venueEvents) {

                    eventTime = event.getUnixTimeMillis();
                    notificationTime = eventTime - AppPref.getTimeMillis();

                    boolean timeCrossing =
                            (lastRecordedTimeLocalCopy <= notificationTime
                                    && notificationTime <= currentTime);

                    boolean timeCrossed =
                            (currentTime >= notificationTime &&
                                    lastRecordedTimeLocalCopy >= notificationTime &&
                                    currentTime <= eventTime);

                    boolean booleanWait;

                    long waitTime = 0;

                    if (eventsNotified.containsKey(event)) {
                        waitTime = currentTime - eventsNotified.get(event) - 20 * 1000 * 60;
                    }

                    if (waitTime < 0) {
                        booleanWait = false;
                    } else {
                        booleanWait = true;
                    }

                    if (event.getId() == 40) {
//                        dlog("Wallace timeCrossing: " + timeCrossing + ",     Wallace timeCrossed: " + timeCrossed);
//                        dlog("currentTime: " + currentTime + " us,     notificatonTime: " + notificationTime + " us");
//                        dlog("lastRecordedTimeLC: " + lastRecordedTimeLocalCopy + " us");
//                        dlog("       currentTime: " + currentTime + " us");
//                        dlog("   notificatonTime: " + notificationTime + " us");
//                        dlog("         eventTime: " + eventTime + " us");
                    }

                    if (timeCrossing && distanceCrossed) {
                        // Notification due to time crossing.
//                        if(booleanWait) {
//                        dlog("Pending new Time Crossing Notification.");
                        return new NotificationInfo(event, true, false);
//                        }

                    } else if (distanceCrossing && timeCrossed) {
                        // Notification due to distance crossing.
//                        dlog("Pending new Distance Crossing Notification.");
                        return new NotificationInfo(event, false, true);

                    } else if (timeCrossing && distanceCrossing) {
                        // Notification due to both time and distance crossing.
//                        dlog("Pending new Distance AND Time Crossing Notification. (unlikely!)");
                        return new NotificationInfo(event, true, true);

                    } else {
                        // No notification should be sent, continue to next iteration.
                    }

                } // End for each of event in venueEvents.

            }

        } // End of for each of venue in this.venues.


        return null;
    }
}
