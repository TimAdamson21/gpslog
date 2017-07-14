package com.example.gpslog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.os.Build;

import java.lang.*;
import java.lang.String;
import java.lang.Integer;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


@SuppressLint("NewApi")
public class MainActivity extends Activity {

    final int FALSE = 0;
    final int TRUE = 1;
    final int UNSTARTED = 0;
    final int WAITINGFORDATA = 1;
    final int STARTED = 2;

    Button startButton;
    Button stopButton;
    Button mapsButton;
    Button svmButton;

    String lat = "";
    String log = "";
    String speed = "";
    //String tag = "";
    //Integer dcrtspd = "";//TODO: double check the "String"
    DatabaseHandler db = null;
    HMMClassifier hmmClassifier;
    SVMClassifier svmClassifier;
    int lastHiddenState = 3; //It starts at 3 so that the first data point will always send
    int startStatus = UNSTARTED;
    private long tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = new DatabaseHandler(this);

        hmmClassifier = new HMMClassifier();
        svmClassifier = new SVMClassifier(db);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        mapsButton = (Button) findViewById(R.id.mapsButton);
        svmButton = (Button) findViewById(R.id.svmButton);

        final LocationManager mylocman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationListener myloclist = new MylocListener();

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (startStatus) {
                    case UNSTARTED:
                        startButton.setText(R.string.waiting_to_start);
                        startStatus = WAITINGFORDATA;
                        tripId = db.getLastTripID() + 1;
                        mylocman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, myloclist);
                        break;
                    case WAITINGFORDATA:
                        mylocman.removeUpdates(myloclist);
                        startButton.setText(R.string.startButton);
                        startStatus = UNSTARTED;
                        break;
                    case STARTED:
                        mylocman.removeUpdates(myloclist);
                        syncSQLiteMySQLDB();
                        startButton.setText(R.string.startButton);
                        startStatus = UNSTARTED;
                        break;
                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mylocman.removeUpdates(myloclist);

                Intent dbmanager = new Intent(getApplicationContext(), AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Track> tracks =(ArrayList)db.getAllTracks();
                if(tracks.size() > 0) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
                }
                else{
                    Message("No data points to view");
                }
            }
        });

        svmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                svmClassifier.classifyStopAndGo();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class MylocListener implements LocationListener {
        @SuppressLint("NewApi")
        public void onLocationChanged(Location loc) {
            lat = loc.getLatitude() + "";
            log = loc.getLongitude() + "";
            speed = loc.getSpeed() + "";
            //tag = null;
            String time = getCurrentTime();
            int hiddenState = hmmClassifier.getHiddenState(loc.getSpeed());
            int toSend = determineToSend(lastHiddenState, hiddenState, time);
            lastHiddenState = hiddenState;
            db.insertRow(time, loc.getLatitude(), loc.getLongitude(),
                    loc.getSpeed(), android.os.Build.SERIAL, hiddenState, toSend, tripId);
            Message("Data Inserted  Latitude:  " + lat + " Longitude: " + log + " Speed: " + speed + " Serial " + android.os.Build.SERIAL + "time: " + time);

            startButton.setText(R.string.started);
            startStatus = STARTED;
        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    private int determineToSend(int lastHiddenState, int hiddenState, String time) {
        int toSend;
        char lastChar = time.charAt(time.length() - 1);
        if(lastHiddenState == HMMClassifier.STOPPED && hiddenState == HMMClassifier.STOPPED){
            toSend = FALSE;
        } else if(lastHiddenState == HMMClassifier.FREEFLOW &&
                hiddenState == HMMClassifier.FREEFLOW && ( lastChar != '0')){ //Used to only
            toSend = FALSE;                                         // send 1 in ten freeflow
        }
        else{
            toSend = TRUE;
        }
        return toSend;
    }

    public void syncSQLiteMySQLDB() {
        /* Create AsycHttpClient object */
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<Track> userList = (ArrayList<Track>) db.getAllTracks();
        if (userList.size() != 0) {
            if (db.dbSyncCount() != 0) {
                params.put("usersJSON", db.composeJSONfromSQLite());
                //Toast.makeText(getApplicationContext(), params.toString(), Toast.LENGTH_LONG).show();
                client.post("http://staff.washington.edu/tda5/insertuser.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println("The length of the response is " + arr.length());
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                System.out.println(obj.get("time"));
                                System.out.println(obj.get("status"));
                                db.updateSyncStatus(obj.get("time").toString(), obj.get("status").toString());
                            }
                            Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        } else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No data in SQLite DB to perform Sync action", Toast.LENGTH_LONG).show();
        }
    }

    public void Message(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Goes through all the Tracks in the database and uses HMM to determine whether or not to send
    //the point. It then sets the toSend column of that track to either 0(false), or 1(true) depending
    //on whether or not it decides to send that point
    private void filterPoints(){
        System.out.println("We are filtering the points");
        ArrayList<Track> tracks =(ArrayList)db.getAllTracks();
        db.setToSend(tracks.get(0).time, 1);
        for(int i = 1; i < tracks.size(); i++){
            Track currTrack = tracks.get(i);
            int currHiddenState = currTrack.hiddenState;
            int prevHiddenState = tracks.get(i-1).hiddenState;
            String time = currTrack.time;
            int toSend = determineToSend(prevHiddenState, currHiddenState, time);
            db.setToSend(currTrack.time, toSend);
        }
    }
    /*
     * Getting the current date time format
     */
    private String getCurrentTime() {
        String time = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        time = df.format(c.getTime());

        return time;
    }

}
