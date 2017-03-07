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

    Button startButton;
    Button stopButton;
    Button syncButton;
    String lat = "";
    String log = "";
    String speed = "";
    //String tag = "";
    //Integer dcrtspd = "";//TODO: double check the "String"
    DatabaseHandler db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = new DatabaseHandler(this);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        syncButton = (Button) findViewById(R.id.syncButton);

        final LocationManager mylocman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationListener myloclist = new MylocListener();

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Message("starting gps");
                mylocman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, myloclist);
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

        syncButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                syncSQLiteMySQLDB();

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
            //Integer dcrtspd = getDiscrtSpeed();
            db.insertRow(time, loc.getLatitude(), loc.getLongitude(), loc.getSpeed(), android.os.Build.SERIAL);
            Message("Data Inserted  Latitude:  " + lat + " Longitude: " + log + " Speed: " + speed + " Serial " + android.os.Build.SERIAL + "time: " + time);

            //updateDatabase();
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
                            System.out.println(arr.length());
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

/*    public Integer getDiscrtSpeed(Double speed) {
        int dcrtspd = 0;

        if (speed <= 1.34112) {    //  3 mph = 1.34112 m/s
            dcrtspd = 0;
        } else if (speed >= 8.9408) {        // 20 mph = 8.9408 m/s
            dcrtspd = 2;
        } else {
            dcrtspd = 1;
        }
        return dcrtspd;
    }
    */
}
