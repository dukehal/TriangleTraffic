package edu.duke.pratt.hal.triangletraffic;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;


public class VenuesActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private ArrayList<Venue> venues;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private HashMap<Venue, TableRow> venueToTableRow = new HashMap<>();
    private HashMap<View, Venue> venueClickRowToVenue = new HashMap<>();
    private boolean tableIsSetup = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_list);

        new DatabaseConnection(this);

        buildGoogleApiClient();

    }

    private TableRow getVenueRow(final Venue venue) {

        //TableRow tableRow = new TableRow(this);

        TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.activity_venue_row, null);
        venueToTableRow.put(venue, tableRow);

        CheckBox notificationCheckbox = (CheckBox) tableRow.findViewById(R.id.notificationCheckBox);
            notificationCheckbox.setChecked(true);
        TextView venueName = (TextView) tableRow.findViewById(R.id.venueName);
        TextView venueDistance = (TextView) tableRow.findViewById(R.id.venueDistance);
        TextView eventTimer = (TextView) tableRow.findViewById(R.id.eventTimer);
        ImageView trafficStatusImage = (ImageView) tableRow.findViewById(R.id.trafficStatusImage);
        LinearLayout venueClickRow = (LinearLayout) tableRow.findViewById(R.id.venueClickRow);


        Drawable statusImage = getResources().getDrawable(R.drawable.traffic_indication_circle);
        // programatically change color:
        statusImage.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);

        trafficStatusImage.setImageDrawable(statusImage);

        ImageView venueInfoLink = (ImageView) tableRow.findViewById(R.id.venueInfoLink);


        venueClickRow.setOnClickListener(this);
        venueClickRowToVenue.put(venueClickRow, venue);

        venueName.setText(venue.getName());
        venueDistance.setText("---");

        if (venue.getPresentEvents().size() > 0) {
            Event nextEvent = venue.nextEvent();
            if (nextEvent !=  null) {
                eventTimer.setText(nextEvent.getLongTimeUntilString());
            } else {
                eventTimer.setText("No upcoming events. (nextEvent null).");
            }
        } else {
            eventTimer.setText("No upcoming events.");

        }

        return tableRow;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_venues, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;
        
        if (!tableIsSetup) {

            this.venues = Venue.sortedVenuesByDistanceTo(currentLocation);

            // Get reference to the Venue Table.
            TableLayout tableLayout = (TableLayout) findViewById(R.id.venueTable);

            // Populate the Venue Table with venue rows.
            for (Venue venue : this.venues) {
                TableRow row = this.getVenueRow(venue);
                tableLayout.addView(row);

                Distance distance = venue.distanceFrom(currentLocation);
                TableRow tableRow = venueToTableRow.get(venue);
                TextView venueDistance = (TextView) tableRow.findViewById(R.id.venueDistance);
                venueDistance.setText(distance.getDisplayString());
            }

            tableIsSetup = true;

        } else {

            for (Venue venue : venues) {
                Distance distance = venue.distanceFrom(currentLocation);
                TableRow tableRow = venueToTableRow.get(venue);
                TextView venueDistance = (TextView) tableRow.findViewById(R.id.venueDistance);
                venueDistance.setText(distance.getDisplayString());
            }

        }


    }

    @Override
    public void onClick(View view) {
        Venue venue = venueClickRowToVenue.get(view);
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("Venue ID", venue.getId());
        startActivity(intent);
    }
}