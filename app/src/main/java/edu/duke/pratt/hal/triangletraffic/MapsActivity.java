package edu.duke.pratt.hal.triangletraffic;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {
    ArrayList<VenueInfo> venues = new ArrayList<VenueInfo>();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        AssetManager assetManager = getAssets();

        // Defined Array values to show in ListView
        InfoRead info = new InfoRead();
        InputStream inputStream = null;

        try {
//            String FILENAME = "hello_file";
//            String string = "hello world!";
//
//            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
//            fos.write(string.getBytes());
//            fos.close();
            inputStream = assetManager.open("InitialExampleDatabase.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            venues.addAll(info.getInfo(inputStreamReader));

//            FileInputStream inputStream = openFileInput("hello_file");
//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            String l;
//            values[0] = "sdlfkj";
//            while ((l = bufferedReader.readLine()) != null) {
//                values[0] = l;
//            }
        } catch(IOException ie) {
            ie.printStackTrace();
        }
//        String[] values = new String[venues.size()];
//
//        for(int i = 0; i<venues.size(); i++) {
//            values[i] = venues.get(i).name();
//        }


        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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

        final LatLng [] positions = new LatLng[venues.size()];
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);

        for(int i = 0; i<venues.size();i++) {
            positions[i] = new LatLng(venues.get(i).lat(), venues.get(i).lon());

            MarkerOptions markerOptions = new MarkerOptions();
            if (!venues.isEmpty()) {
                markerOptions.position(positions[i])
                        .title(venues.get(i).name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
            } else {
                markerOptions.position(positions[i])
                        .title("List not working")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball_ball));
            }

            Marker marker = mMap.addMarker(markerOptions);

            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(positions[i])
                    .radius(venues.get(i).trafficLevel()*1000) // In meters
                    .strokeColor(Color.RED)
                    .strokeWidth(5)
                    .fillColor(0x50ff0000);
            // Get back the mutable Circle
            Circle circle = mMap.addCircle(circleOptions);
        }

    }
}