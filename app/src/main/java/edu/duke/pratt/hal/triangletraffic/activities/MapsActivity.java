package edu.duke.pratt.hal.triangletraffic.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.duke.pratt.hal.triangletraffic.PreferenceConnection;
import edu.duke.pratt.hal.triangletraffic.model.NotificationInfo;
import edu.duke.pratt.hal.triangletraffic.R;
import edu.duke.pratt.hal.triangletraffic.model.Event;
import edu.duke.pratt.hal.triangletraffic.model.Venue;
import edu.duke.pratt.hal.triangletraffic.utility.AppPref;
import edu.duke.pratt.hal.triangletraffic.utility.DatabaseConnection;
import edu.duke.pratt.hal.triangletraffic.utility.Distance;

public class MapsActivity extends ActionBarActivity implements OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    ArrayList<Venue> venues;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public ArrayList<Marker> myMarkers = new ArrayList<>();
    HashMap <String, Integer> mMarkers = new HashMap<>();
    HashMap<Event, Long> eventsNotified = new HashMap<>();
    HashMap<Marker, Venue> markerToVenue = new HashMap<>();
    GoogleApiClient client;
    Location location;
    private LocationRequest locationRequest;
    Location currentLocation;
    //double radiusPref;
    //long timePref;
    boolean audioPref;
    boolean textPref;
    boolean vibratePref;
    private long lastRecordedTime;
    private Location lastRecordedLocation;
    private int locationUpdateCount = 0;
    private Switch trafficSwitch;

    //SharedPreferences sharedPref;

    private TextView dlog;
    private boolean googleApiClientconnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default values for settings.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_maps);

        initializeDisplayLog();

        buildGoogleApiClient();


        //radiusPref = AppPref.getRadiusMeters();
        //timePref = AppPref.getTimeMillis();

        new DatabaseConnection(this);
        new PreferenceConnection(this);

        venues = Venue.asArrayList();


//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(getApplicationContext())
//                                .setSmallIcon(R.drawable.basketball_ball)
//                                .setContentTitle("My notification 430")
//                                .setContentText("Hello World!")
//                                .setTicker("TriangleTrafficApp");
//
//                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                if (alarmSound == null) {
//                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                    if (alarmSound == null) {
//                        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    }
//                }
//
//                mBuilder.setSound(alarmSound);
//                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//                mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//
//                int mNotificationId = 001;
//// Gets an instance of the NotificationManager service
//                NotificationManager mNotifyMgr =
//                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
//// Builds the notification and issues it.
//                mNotifyMgr.notify(mNotificationId, mBuilder.build());
//            }
//        }, 8000);

    }

    private void initializeDisplayLog() {
        dlog = (TextView) findViewById(R.id.dlog);
        dlog.setMovementMethod(new ScrollingMovementMethod());
        dlog("Display Log Initialized.");
        ToggleButton toggleDiagnostics = (ToggleButton) findViewById(R.id.toggle_diagnostics);
        toggleDiagnostics.setOnClickListener(this);

    }

    private void dlog(String text) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("h:mm:ss");
        String pre = f.format(cal.getTime());
        dlog.append(" (" + locationUpdateCount + ") " + pre + "  " + text + "\n");
        dlog.post(new Runnable() {
            public void run() {
                final int scrollAmount = dlog.getLayout().getLineTop(dlog.getLineCount()) - dlog.getHeight();
                if (scrollAmount > 0) {
                    dlog.scrollTo(0, scrollAmount);
                } else {
                    dlog.scrollTo(0, 0);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.w("dbug", "ONRESUME Called");

        if (googleApiClientconnected) {
            setUpMap();
        }

        // Settings could have changed. Setup the map again.
        client.connect();
    }

    protected void onPause() {
        super.onPause();
        Log.w("dbug", "ONPAUSE Called");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.TriangleTraffic))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.clear();
        location = LocationServices.FusedLocationApi.getLastLocation(client);
        if (location == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 0));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));

        }

        trafficSwitch = (Switch) findViewById(R.id.trafficSwitch);
        trafficSwitch.setChecked(true);
        trafficSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMap.setTrafficEnabled(true);
                } else {
                    mMap.setTrafficEnabled(false);
                }
            }
        });

        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

        for (Venue venue : venues) {

            MarkerOptions markerOptions = new MarkerOptions();
            if (!venues.isEmpty()) {
                if (venue.getVenueType().equals("Arena") || venue.getVenueType().equals("Stadium")) {
                    markerOptions
                            .position(venue.getLatLng())
                            .title(venue.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
                } else if (venue.getVenueType().equals("Concert Hall") || venue.getVenueType().equals("Theater")) {
                    markerOptions
                            .position(venue.getLatLng())
                            .title(venue.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.theater));
                } else {
                    markerOptions
                            .position(venue.getLatLng())
                            .title("Arena type not working!!")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
                }
            } else {
                markerOptions
                        .position(venue.getLatLng())
                        .title("List not working")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
            }

            Marker marker = mMap.addMarker(markerOptions);
            myMarkers.add(marker);
            markerToVenue.put(marker, venue);

            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(venue.getLatLng())
                    .radius(Math.abs(AppPref.getRadiusMeters())) // In meters
                    .strokeColor(venue.getCircleStrokeColor())
                    .strokeWidth(3)
                    .fillColor(venue.getCircleFillColor());
            // Get back the mutable Circle
            mMap.addCircle(circleOptions);
        }
    }

    @Override
    public boolean onMarkerClick(Marker myMarker) {
        boolean isMarker = false;

        int i = 0;
        while(i<myMarkers.size()) {
            if(myMarker.equals(myMarkers.get(i))) {
                isMarker = true;
                myMarker.getId();
                break;
            }
            i++;
        }

        if (isMarker) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra("Venue ID", markerToVenue.get(myMarker).getId());
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_venues) {
            Intent intent = new Intent(this, VenuesActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_feedback) {
            Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    public GoogleApiClient getClient() {
        return client;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        googleApiClientconnected = true;
        Log.w("dbug", "in ONCONECTED.");
        if(location != null) {
            Log.w("info", Double.toString(location.getLatitude()));
            Log.w("info", Double.toString(location.getLongitude()));
        } else {
            Log.w("info", "Unable to get location coordinates");
        }
        createLocationRequest();
        startLocationUpdates();
        setUpMapIfNeeded();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        locationUpdateCount++;

        currentLocation = location;
        if (lastRecordedLocation == null) {
            lastRecordedLocation = location;
        }

        Log.w("current lat", Double.toString(currentLocation.getLatitude()));
        Log.w("current long", Double.toString(currentLocation.getLongitude()));
        //sendMockNotification("Location changed: " + Double.toString(currentLocation.getLatitude()));

        Float dist = lastRecordedLocation.distanceTo(currentLocation);
        Float bear = lastRecordedLocation.bearingTo(currentLocation);

        String distanceString = (new Distance(dist)).getDisplayString();
        String logMessage = String.format("(lat, lon): %3.7f, %3.7f    \u0394(dist, brg): %s, % 7.3f deg.",
                currentLocation.getLatitude(), currentLocation.getLongitude(), distanceString, bear);

        dlog(logMessage);

        NotificationInfo notificationToSend = shouldSendNotification();
        Event event;

        if (notificationToSend != null) {

            event = notificationToSend.getEvent();

            if (notificationToSend.isDueToTimeCrossing()) {
                dlog("sending time crossing notification...");
                sendMockNotification("An event is due to occur in your location: " + event.getName());
            } else if (notificationToSend.isDueToDistanceCrossing()) {
                dlog("sending distance crossing notification...");
                sendMockNotification("You crossed an area where an event is due to occur: " + event.getName());
            }

        }

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
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.basketball_ball)
                                .setContentTitle("Triangle Traffic")
                                .setContentText(notificationText)
                                .setTicker(notificationText);

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                if (alarmSound == null) {
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    if (alarmSound == null) {
                        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }
                }

                if (textPref) {
                    mBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                }

                if (audioPref) {
                    mBuilder.setSound(alarmSound);
                }

                if (vibratePref) {
                    mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                }

                int mNotificationId = event.getVenueId();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        }
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
                    dlog("Wallace distanceCrossing: " + distanceCrossing + ",     Wallace distanceCrossed: " + distanceCrossed);
                    String lastVsCurr = String.format("lastRecordedDistance: %.2f m,     currentDistance: %.2f m",
                            lastRecordedDistance.getMeters(), currentDistance.getMeters());
                    dlog(lastVsCurr);
                    dlog("notificationRadius: " + notificationRadius + " m");
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

                    if (event.getId() == 40) {
                        dlog("Wallace timeCrossing: " + timeCrossing + ",     Wallace timeCrossed: " + timeCrossed);
                        dlog("currentTime: " + currentTime + " us,     notificatonTime: " + notificationTime + " us");
//                        dlog("lastRecordedTimeLC: " + lastRecordedTimeLocalCopy + " us");
//                        dlog("       currentTime: " + currentTime + " us");
//                        dlog("   notificatonTime: " + notificationTime + " us");
//                        dlog("         eventTime: " + eventTime + " us");
                    }

                    if (timeCrossing && distanceCrossed) {
                        // Notification due to time crossing.

                        dlog("Pending new Time Crossing Notification.");
                        return new NotificationInfo(event, true, false);

                    } else if (distanceCrossing && timeCrossed) {
                        // Notification due to distance crossing.
                        dlog("Pending new Distance Crossing Notification.");
                        return new NotificationInfo(event, false, true);

                    } else if (timeCrossing && distanceCrossing) {
                        // Notification due to both time and distance crossing.
                        dlog("Pending new Distance AND Time Crossing Notification. (unlikely!)");
                        return new NotificationInfo(event, true, true);

                    } else {
                        return new NotificationInfo(null, false, false);
                        // No notification should be sent, continue to next iteration.
                    }

                } // End for each of event in venueEvents.

            }

        } // End of for each of venue in this.venues.


        return null;
    }

    private void sendMockNotification(String text) {
        final TextView disp = (TextView) findViewById(R.id.mockNotificationDisplay);
        disp.setText(text);
        disp.setVisibility(View.VISIBLE);
        disp.postDelayed(new Runnable() {
            @Override
            public void run() {
                disp.setVisibility(View.INVISIBLE);
            }
        }, 10000);

        // Play a notification sound.
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.toggle_diagnostics) {
            ToggleButton toggleDiagnostics = (ToggleButton) view;
            dlog.setVisibility(toggleDiagnostics.isChecked() ? View.VISIBLE : View.GONE);
        }
    }
}
