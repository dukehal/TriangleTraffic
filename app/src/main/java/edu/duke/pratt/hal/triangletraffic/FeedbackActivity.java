package edu.duke.pratt.hal.triangletraffic;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class FeedbackActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Location currentLocation;
    GoogleApiClient client;
    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        buildGoogleApiClient();
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
        Log.w("current lat feedback", Double.toString(currentLocation.getLatitude()));
        Log.w("current long feedback", Double.toString(currentLocation.getLongitude()));
    }

    public void sendFeedback(View view) {

        final Handler handler = new Handler(Looper.getMainLooper());

        Runnable tryGetLocation = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    Log.w("dbug", "Location set, creating file");
                    generateFile();
                } else {
                    Log.w("dbug", "Location not set");
                    handler.postDelayed(this, 100);
                }
            }
        };

        handler.postDelayed(tryGetLocation, 100);
    }

    private void generateFile(){

        File attachmentsPath = new File(this.getCacheDir(), "attachments");
        attachmentsPath.mkdirs();

        File feedbackData = new File(attachmentsPath, "UserFeedback_" + System.currentTimeMillis() + ".txt");

        try {
            feedbackData.createNewFile();

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(feedbackData);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                try {
                    bw.write("Timestamp: " + System.currentTimeMillis());
                    bw.newLine();
                    bw.newLine();
                    bw.write("BUILD INFORMATION:");
                    bw.write("Build CODENAME: " + Build.VERSION.CODENAME);
                    bw.newLine();
                    bw.write("Build INCREMENTAL: " + Build.VERSION.INCREMENTAL);
                    bw.newLine();
                    bw.write("Build RELEASE: " + Build.VERSION.RELEASE);
                    bw.newLine();
                    bw.write("BOARD: " + Build.BOARD);
                    bw.newLine();
                    bw.write("BOOTLOADER: " + Build.BOOTLOADER);
                    bw.newLine();
                    bw.write("BOARD: " + Build.BOARD);
                    bw.newLine();
                    bw.write("BRAND: " + Build.BRAND);
                    bw.newLine();
                    bw.write("DEVICE: " + Build.DEVICE);
                    bw.newLine();
                    bw.write("DISPLAY: " + Build.DISPLAY);
                    bw.newLine();
                    bw.write("FINGERPRINT: " + Build.FINGERPRINT);
                    bw.newLine();
                    bw.write("HARDWARE: " + Build.HARDWARE);
                    bw.newLine();
                    bw.write("HOST: " + Build.HOST);
                    bw.newLine();
                    bw.write("ID: " + Build.ID);
                    bw.newLine();
                    bw.write("MANUFACTURER: " + Build.MANUFACTURER);
                    bw.newLine();
                    bw.write("MODEL: " + Build.MODEL);
                    bw.newLine();
                    bw.write("PRODUCT: " + Build.PRODUCT);
                    bw.newLine();
                    bw.write("SERIAL NUMBER: " + Build.SERIAL);
                    bw.newLine();
                    bw.write("TAGS: " + Build.TAGS);
                    bw.newLine();
                    bw.write("TYPE: " + Build.TYPE);
                    bw.newLine();
                    bw.write("USER: " + Build.USER);
                    bw.newLine();
                    bw.newLine();
                    bw.write("LOCATION:");
                    bw.newLine();
                    bw.write("Latitude:  " + Double.toString(currentLocation.getLatitude()));
                    bw.newLine();
                    bw.write("Longitude:  " + Double.toString(currentLocation.getLongitude()));
                    bw.newLine();
                    bw.write("Accuracy:  " + Float.toString(currentLocation.getAccuracy()));
                    bw.newLine();
                    bw.write("Provider:  " + currentLocation.getProvider());
                    bw.newLine();
                    bw.write("Speed:  " + Float.toString(currentLocation.getSpeed()));
                    bw.newLine();
                    bw.write("Bearing:  " + Float.toString(currentLocation.getBearing()));
                    bw.close();
                } catch (IOException e) {
                    // Log.w("dbug:warn", "BufferedWriter coudln't write, flush, or close.");
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                // Log.w("dbug:warn", "feedbackData coudln't create new file.");
                e.printStackTrace();
            }

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri contentUri = FileProvider.getUriForFile(this, "edu.duke.pratt.hal.triangletraffic.fileprovider", feedbackData);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"joshua.furth@duke.edu"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TriangleTraffic App User Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Thanks for using TriangleTraffic!\nPlease share your feedback, comments, and feature requests below:\n\n");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_feedback) {
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_venues) {
            Intent intent = new Intent(this, VenuesActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
