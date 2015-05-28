package edu.duke.pratt.hal.triangletraffic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class InfoActivity extends ActionBarActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    VenueInfo eventMarker;
    Location currentLocation;
    GoogleApiClient client;
    ArrayList<VenueInfo> venues = new ArrayList<VenueInfo>();
    ArrayList<EventInfo> events = new ArrayList<EventInfo>();
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_window);

        venues = (ArrayList<VenueInfo>)getIntent().getSerializableExtra("Venue_Information");
        int i = getIntent().getIntExtra("Marker", 0);
        eventMarker = venues.get(i);
        buildGoogleApiClient();
        TextView t = (TextView)findViewById(R.id.textView);
        t.setText(eventMarker.name());
        t = (TextView)findViewById(R.id.textView7);
        t.setText(eventMarker.type());
        t = (TextView)findViewById(R.id.textView8);
        if(eventMarker.type() == "0") {
            t.setText("None");
        }
        else {
            t.setText(eventMarker.assoc());
        }
        t = (TextView)findViewById(R.id.textView10);
        t.setText(eventMarker.address());

        createLocationRequest();
    }

    @Override
    public void onMapReady(GoogleMap map) {
//        currentLocation = getCurrentLocation();
//        if(currentLocation == null) {
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 16));
//        } else {
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),
//                    currentLocation.getLongitude()), 16));
//        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(eventMarker.lat(), eventMarker.lon()), 16));
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(eventMarker.lat(),eventMarker.lon()))
                .title(eventMarker.name()))
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

//    private Location getCurrentLocation() {
//        return LocationServices.FusedLocationApi.getLastLocation(client);
//    }

//    private VenueInfo getMarkerInfo() {
//        MapsActivity activity = (MapsActivity) getParent();
//        return
//    }

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
    }
}
