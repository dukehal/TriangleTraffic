package edu.duke.pratt.hal.triangletraffic;

import android.content.Intent;
import android.location.Location;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


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
        TextView t = (TextView)findViewById(R.id.textView);
        t.setText(venueInfo.getName());
        t = (TextView)findViewById(R.id.textView7);
        t.setText(venueInfo.getVenueType());
        t = (TextView)findViewById(R.id.textView8);
        if(venueInfo.getVenueType() == "0") {
            t.setText("None");
        }
        else {
            t.setText(venueInfo.getAssociation());
        }
        t = (TextView)findViewById(R.id.textView10);
        t.setText(venueInfo.getAddress());

//      load events in scrollable table:
//        XmlPullParser parser = getResources().getXml(R.xml.event_data_attributes);
//        AttributeSet attributes = Xml.asAttributeSet(parser);

        ArrayList<Event> eventInfoList = venueInfo.getEvents();
        TableLayout eventTable = (TableLayout)findViewById(R.id.eventTable);
        for (int i = 0; i < eventInfoList.size(); i++) {

            TableRow eventRow = new TableRow(this);
            TableRow.LayoutParams eventRowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,1f);
//            int leftMargin=10;
//            int topMargin=2;
//            int rightMargin=10;
//            int bottomMargin=2;
//            eventRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            eventRow.setLayoutParams(eventRowParams);

            Event eventInfo = eventInfoList.get(i);

            TextView timeUntil = new TextView(this);
            timeUntil.setText(eventInfo.getTime());
            timeUntil.setTextSize(20);
            timeUntil.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 3f));
            eventRow.addView(timeUntil);

            TextView eventDate = new TextView(this);
            eventDate.setText(eventInfo.getDate());
            eventDate.setTextSize(20);
            eventDate.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 3f));
            eventRow.addView(eventDate);

            TextView eventTime = new TextView(this);
            eventTime.setText(eventInfo.getTime());
            eventTime.setTextSize(20);
            eventTime.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 3f));
            eventRow.addView(eventTime);

            TextView eventName = new TextView(this);
            eventName.setText(eventInfo.getName());
            eventName.setTextSize(20);
            eventName.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0.5f));
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
        map.addMarker(new MarkerOptions()
                .position(new LatLng(venueInfo.getLatitude(), venueInfo.getLongitude()))
                .title(venueInfo.getName()))
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
    }


}
