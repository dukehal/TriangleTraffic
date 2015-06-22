package edu.duke.pratt.hal.triangletraffic.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.duke.pratt.hal.triangletraffic.R;
import edu.duke.pratt.hal.triangletraffic.model.Event;
import edu.duke.pratt.hal.triangletraffic.model.Venue;
import edu.duke.pratt.hal.triangletraffic.utility.DatabaseConnection;
import edu.duke.pratt.hal.triangletraffic.utility.Distance;


public class VenuesActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ArrayList<Venue> venuesDistance;
    private ArrayList<Venue> venuesAlphabet;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private HashMap<Venue, TableRow> venueToTableRow = new HashMap<>();
    private HashMap<View, Venue> venueClickRowToVenue = new HashMap<>();
    private boolean sortByDistance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new DatabaseConnection(this);

        buildGoogleApiClient();

        Spinner spinner = (Spinner) findViewById(R.id.sortBy_spinner);
        spinner.setOnItemSelectedListener(this);

        sortByDistance = true;

        populateSortBySpinner();

        venuesAlphabet = Venue.sortedVenuesByAlphabet();
        //updateTable(venuesAlphabet);

    }

    public void onVenueNotificationsClick(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        for(Map.Entry<Venue, TableRow> entry : venueToTableRow.entrySet()) {
            Venue key = entry.getKey();
            TableRow value = entry.getValue();

            CheckBox checkBox = (CheckBox) value.findViewById(R.id.notificationCheckBox);
            checkBox.setChecked(on);
        }
    }

    public void populateSortBySpinner() {
        Spinner sortBySpinner = (Spinner) findViewById(R.id.sortBy_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sortBy_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sortBySpinner.setAdapter(adapter);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (parent.getItemAtPosition(pos).toString().equals("Distance")) {
            Log.w("dbug", "Distance selected");
            //Log.w("dbug", parent.getItemAtPosition(pos).toString());
            //updateTable(venuesDistance);
            sortByDistance = true;
        }
        if (parent.getItemAtPosition(pos).toString().equals("Alphabet")) {
            Log.w("dbug", "Alphabet selected");
            //updateTable(venuesAlphabet);
            sortByDistance = false;
        }
        updateTable();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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
        statusImage.setColorFilter(venue.getCircleStrokeColor(), PorterDuff.Mode.SRC_ATOP);

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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else if (id == android.R.id.home) {
            this.finish();
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

        venuesDistance = Venue.sortedVenuesByDistanceTo(currentLocation);

        updateTable();



    }

    @Override
    public void onClick(View view) {
        Venue venue = venueClickRowToVenue.get(view);
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("Venue ID", venue.getId());
        startActivity(intent);
    }

    public void updateTable () {

        // Get reference to the Venue Table.
        TableLayout tableLayout = (TableLayout) findViewById(R.id.venueTable);
        tableLayout.removeAllViews();

        ArrayList<Venue> venues;
        if (sortByDistance) {
            if (venuesDistance == null) {
                venuesDistance = Venue.sortedVenuesByDistanceTo(LocationServices.FusedLocationApi.getLastLocation(client));
            }
            venues = venuesDistance;
        } else {
            venues = venuesAlphabet;
        }

        // Populate the Venue Table with venue rows.
        for (Venue venue : venues) {
            TableRow row = this.getVenueRow(venue);
            tableLayout.addView(row);
            Distance distance;

            if (currentLocation == null) {
                distance = new Distance(0);
            } else {
                distance = venue.distanceFrom(currentLocation);
            }

            TableRow tableRow = venueToTableRow.get(venue);
            TextView venueDistance = (TextView) tableRow.findViewById(R.id.venueDistance);
            venueDistance.setText(distance.getDisplayString());
        }

        // Remove the Progress Bar.
        View venueListLoading = (View) findViewById(R.id.venueListLoading);
        venueListLoading.setVisibility(View.GONE);
    }
}