package edu.duke.pratt.hal.triangletraffic;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;


public class InfoActivity extends ActionBarActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Venue venueInfo;
    Location currentLocation;
    GoogleApiClient client;
    ArrayList<Venue> venues;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);

        new DatabaseConnection(this);

        venues = Venue.asArrayList();

        int venueIndex = getIntent().getIntExtra("Marker", 0);
        venueInfo = venues.get(venueIndex);

        buildGoogleApiClient();
        TextView t = (TextView)findViewById(R.id.venueName);
        t.setText(venueInfo.getName());
        t = (TextView)findViewById(R.id.venueType);
        t.setText(venueInfo.getVenueType());
        t = (TextView)findViewById(R.id.venueAffiliation);
        if(!venueInfo.getAssociation().equals("0")) {
            t.setText(venueInfo.getAssociation());
        }
        else {
            t.setText("None");
        }
        t = (TextView)findViewById(R.id.venueAddress);
        t.setText(venueInfo.getAddress());

        ArrayList<Event> eventInfoList = venueInfo.getEvents();
        Collections.sort(eventInfoList);
        TableLayout eventTable = (TableLayout)findViewById(R.id.eventTable);
        for (int i = 0; i < eventInfoList.size(); i++) {

            TableRow eventRow = new TableRow(this);
            TableRow.LayoutParams eventRowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,1f);

            eventRow.setLayoutParams(eventRowParams);

            Event eventInfo = eventInfoList.get(i);

            TextView timeUntil = new TextView(this);
            timeUntil.setText(eventInfo.getTimeUntilString());
            timeUntil.setTextSize(20);
            timeUntil.setGravity(Gravity.CENTER);
            timeUntil.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 1f));
            eventRow.addView(timeUntil);

            TextView eventDate = new TextView(this);
            eventDate.setText(eventInfo.getDateString());
            eventDate.setTextSize(20);
            eventDate.setGravity(Gravity.CENTER);
            eventDate.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 1f));
            eventRow.addView(eventDate);

            TextView eventTime = new TextView(this);
            eventTime.setText(eventInfo.getTimeString());
            eventTime.setTextSize(20);
            eventTime.setGravity(Gravity.CENTER);
            eventTime.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 1f));
            eventRow.addView(eventTime);

            TextView eventName = new TextView(this);
            eventName.setText(eventInfo.getName());
            eventName.setTextSize(20);
            eventName.setGravity(Gravity.CENTER);
            eventName.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 1f));
            eventRow.addView(eventName);

            eventTable.addView(eventRow);

        }
        createLocationRequest();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venueInfo.getLatitude(), venueInfo.getLongitude()), 16));
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);

        MarkerOptions markerOptions = new MarkerOptions();
        if (venueInfo.getVenueType().equals("Arena") || venueInfo.getVenueType().equals("Stadium")) {
            markerOptions.position(new LatLng(venueInfo.getLatitude(), venueInfo.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
            markerOptions.title(venueInfo.getName());
        } else if (venueInfo.getVenueType().equals("Concert Hall") || venueInfo.getVenueType().equals("Theater")) {
            markerOptions.position(new LatLng(venueInfo.getLatitude(), venueInfo.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theater));
            markerOptions.title(venueInfo.getName());
        } else {
            markerOptions.position(new LatLng(venueInfo.getLatitude(), venueInfo.getLongitude()));
            markerOptions.title("Arena type not working!!");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
        }


        map.addMarker(markerOptions)
                .showInfoWindow();
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

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

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

        float[] results = new float[2];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), venueInfo.getLatitude(), venueInfo.getLongitude(), results);

        Distance distance = new Distance(results[0]);

        TextView distanceValue = (TextView)findViewById(R.id.distanceValue);

        distanceValue.setText(distance.getDisplayString());
        distanceValue.setTextColor(Color.parseColor("#000000"));

    }


}
