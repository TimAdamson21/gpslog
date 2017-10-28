package com.example.gpslog;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHandler db;
    private Spinner trips_spinner;
    final int zoomLevel = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        db = new DatabaseHandler(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<Track> userList = (ArrayList<Track>) db.getAllTracks(); //Gets list of all Tracks in database

        // Add a marker in Sydney and move the camera
        for (Track track: userList) {
            float color = BitmapDescriptorFactory.HUE_RED;
            float opacity = 1.0f;
            if(track.hiddenState == 1){
                color = BitmapDescriptorFactory.HUE_YELLOW;
            } else if(track.hiddenState == 2){
                color = BitmapDescriptorFactory.HUE_GREEN;
            }

            if(track.realTimeToSend == 0){
                opacity = 0.25f;
            }

            mMap.addMarker(new MarkerOptions().position(new LatLng(track.latitude,track.longitude)).
                    title(track.time).alpha(opacity).icon(BitmapDescriptorFactory.defaultMarker(color)));

        }
        Track lastTrack = userList.get(userList.size()-1);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastTrack.latitude,lastTrack.longitude),zoomLevel));
    }
 ////////////// The stuff bellow this line is still in the dev phase
    public void addItemsOnSpinner(){
        //trips_spinner = (Spinner) findViewById(R.id.trips_spinner);
        List<String> list = db.getTripIDs();
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //trips_spinner.setAdapter(dataAdapter);
    }

}
