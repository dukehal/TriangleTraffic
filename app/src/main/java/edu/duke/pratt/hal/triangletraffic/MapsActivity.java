package edu.duke.pratt.hal.triangletraffic;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MapsActivity extends ActionBarActivity implements OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    ArrayList<Venue> venues;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public ArrayList<Marker> myMarkers = new ArrayList<>();
    HashMap <String, Integer> mMarkers = new HashMap<String, Integer>();
    HashMap<Event, Long> eventsNotified = new HashMap<Event, Long>();
    GoogleApiClient client;
    Location location;
    private LocationRequest locationRequest;
    Location currentLocation;
    double radiusPref;
    long timePref;
    boolean audioPref;
    boolean textPref;
    boolean vibratePref;
    private long lastRecordedTime;
    private Location lastRecordedLocation;

    SharedPreferences sharedPref;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private TextView dlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        dlog = (TextView) findViewById(R.id.dlog);
        dlog.setMovementMethod(new ScrollingMovementMethod());

        dlog("Hello 2!");

        buildGoogleApiClient();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        radiusPref = 1609.34*Double.parseDouble(sharedPref.getString("radius_list", "0"));

        new DatabaseConnection(this);
        venues = Venue.asArrayList();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                sharedPref = prefs;
                sharedPref.registerOnSharedPreferenceChangeListener(this);
                if (key.equals("radius_list")) {
//                    Log.w("key equals radius list", Integer.toString(radiusPref));
                    radiusPref = Double.parseDouble(sharedPref.getString("radius_list", "0"))*
                            1609.34;
                    Log.w("keyequalsradius2", Double.toString(radiusPref));
                    setUpMap();
                }
                Log.w("are you getting this?", Double.toString(radiusPref));

                if (key.equals("timing_list")) {
                    int time = Integer.parseInt(sharedPref.getString("timing_list", "60"));
                    timePref = time*60*1000;
                }

                if(key.equals("text_mode")) {
                    textPref = Boolean.parseBoolean(sharedPref.getString("text_mode", "true"));
                }

                if(key.equals("audio_mode")) {
                    audioPref = Boolean.parseBoolean(sharedPref.getString("audio_mode", "true"));
                }

                if(key.equals("vibrate_mode")) {
                    vibratePref = Boolean.parseBoolean(sharedPref.getString("vibrate_mode", "true"));
                }
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    private void dlog(String text) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("h:mm:ss");
        String pre = f.format(cal.getTime());
        dlog.append(" " + pre + "  " + text + "\n");
    }


    @Override
    protected void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        client.connect();
    }

    protected void onPause() {
        super.onPause();
//        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));
        final LatLng [] positions = new LatLng[venues.size()];
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

//        Log.w("radius", Integer.toString(radiusPref));
//        Log.w("radius2", sharedPref.getString("radius_list", "0"));
//        int radiusPref = 1;

        for(int i = 0; i<venues.size();i++) {
            positions[i] = new LatLng(venues.get(i).getLatitude(), venues.get(i).getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            if (!venues.isEmpty()) {
                if (venues.get(i).getVenueType().equals("Arena") || venues.get(i).getVenueType().equals("Stadium")) {
                    markerOptions.position(positions[i])
                            .title(venues.get(i).getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
                } else if (venues.get(i).getVenueType().equals("Concert Hall") || venues.get(i).getVenueType().equals("Theater")) {
                    markerOptions.position(positions[i])
                            .title(venues.get(i).getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.theater));
                } else {
                    markerOptions.position(positions[i])
                            .title("Arena type not working!!")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
                }
            } else {
                markerOptions.position(positions[i])
                        .title("List not working")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
            }

            Marker marker = mMap.addMarker(markerOptions);
            myMarkers.add(marker);

            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(positions[i])
                    .radius(Math.abs(radiusPref)) // In meters
                    .strokeColor(Color.RED)
                    .strokeWidth(5)
                    .fillColor(0x50ff0000);
            // Get back the mutable Circle
            Circle circle = mMap.addCircle(circleOptions);
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
            intent.putExtra("Marker", i);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}

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
        currentLocation = location;
        Log.w("current lat", Double.toString(currentLocation.getLatitude()));
        Log.w("current long", Double.toString(currentLocation.getLongitude()));

        Float dist = lastRecordedLocation.distanceTo(currentLocation);
        Float bear = lastRecordedLocation.bearingTo(currentLocation);

        String distanceString = (new Distance(dist)).getDisplayString();
        String bearingString = bear + " deg.";

        dlog("(lat, lon): " + Double.toString(currentLocation.getLatitude()) +
                ", " + Double.toString(currentLocation.getLongitude()) +
                "    (dist, brg): " + distanceString + ", " + bearingString);

        if(lastRecordedLocation == null) {
            lastRecordedLocation = currentLocation;
            lastRecordedTime = System.currentTimeMillis();
        } else {
            Event eventNotified = shouldSendNotification();
            if (eventNotified != null) {
                StringBuilder notificationText = new StringBuilder();
                notificationText.append(eventNotified.getName());
                notificationText.append(" has an event today at ");
                notificationText.append(eventNotified.getTimeString());
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

                int mNotificationId = eventNotified.getVenueId();
// Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
// Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        }
    }

    public Event shouldSendNotification() {
        long eventTime;
        long notificationTime;
        long currentTime = System.currentTimeMillis();
        float[] results = new float[2];
        Double notificationRadius = radiusPref;
        Event eventNotified = null;

        for (Venue venue : venues) {
            if (venue.getNotification()) {
                ArrayList<Event> events = venue.getEvents();
                Location.distanceBetween(lastRecordedLocation.getLatitude(), lastRecordedLocation.getLongitude(),
                        venue.getLatitude(), venue.getLongitude(),results);
                Distance lastRecordedDistance = new Distance(results[0]);

                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        venue.getLatitude(), venue.getLongitude(),results);
                Distance currentDistance = new Distance(results[0]);

                boolean distanceCrossing = (lastRecordedDistance.getMeters() >= notificationRadius &&
                        currentDistance.getMeters() <= notificationRadius);
                boolean distanceCrossed = (lastRecordedDistance.getMeters() <= notificationRadius &&
                        currentDistance.getMeters() <= notificationRadius);

                for (Event event : events) {
                    eventTime = event.getUnixTimeMillis();
                    notificationTime = eventTime - timePref*60*1000;
                    boolean timeCrossing = (lastRecordedTime <= notificationTime
                            && notificationTime <= currentTime);
                    boolean timeCrossed = (currentTime >= notificationTime &&
                            lastRecordedTime >= notificationTime && currentTime <= eventTime);
                    if (timeCrossing && distanceCrossed) {
//                        if(eventsNotified.containsKey(event)) {
//                            if(currentTime >= eventsNotified.get(event) + 10*60*1000) {
//                                eventsNotified.put(event, currentTime);
//                            } else {
//                                eventNotified = event;
//                            }
//                        } else {
                            eventsNotified.put(event, currentTime);
                            eventNotified = event;
//                        }
                    } else if (distanceCrossing && timeCrossed) {
//                        if(eventsNotified.containsKey(event)) {
//                            if(currentTime >= eventsNotified.get(event) + 10*60*1000) {
//                                eventsNotified.put(event, currentTime);
//                            } else {
//                                eventNotified = event;
//                            }
//                        } else {
                            eventsNotified.put(event, currentTime);
                            eventNotified = event;
//                        }
                    } else if (timeCrossing && distanceCrossing) {
//                        if(eventsNotified.containsKey(event)) {
//                            if(currentTime >= eventsNotified.get(event) + 10*60*1000) {
//                                eventsNotified.put(event, currentTime);
//                            } else {
//                                eventNotified = event;
//                            }
//                        } else {
                            eventsNotified.put(event, currentTime);
                            eventNotified = event;
//                        }
                    } else {
                        eventNotified = null;
                    }
                }
            }
        }

        lastRecordedTime = currentTime;
        lastRecordedLocation = currentLocation;
        return eventNotified;
    }
}












//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(getApplicationContext())
//                                .setSmallIcon(R.drawable.basketball_ball)
//                                .setContentTitle("My notification 430")
//                                .setContentText("Hello World!");
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