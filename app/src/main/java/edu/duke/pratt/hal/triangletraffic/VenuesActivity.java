package edu.duke.pratt.hal.triangletraffic;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;


public class VenuesActivity extends ActionBarActivity {

    private ArrayList<Venue> venues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_list);

        new DatabaseConnection(this);
        
        this.venues = Venue.asArrayList();

        // Get reference to the Venue Table.
        TableLayout tableLayout = (TableLayout) findViewById(R.id.venueTable);

        // Populate the Venue Table with venue rows.
        for (Venue venue : this.venues) {
            TableRow row = this.getVenueRow(venue);
            tableLayout.addView(row);
        }

    }

    private TableRow getVenueRow(Venue venue) {

        TableRow tableRow = new TableRow(this);

        // Add ToggleButton.
        ToggleButton toggle = new ToggleButton(this);
        tableRow.addView(toggle);

        // Add Name.
        TextView name = new TextView(this);
        name.setText(venue.getName());
        tableRow.addView(name);

        // Add Association.
        TextView association = new TextView(this);
        association.setText(venue.getAssociation());
        tableRow.addView(association);


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
}
