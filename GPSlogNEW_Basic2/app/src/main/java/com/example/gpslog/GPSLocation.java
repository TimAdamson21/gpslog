package com.example.gpslog;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSLocation extends Activity {
	
	String lat="";
	String log="";
	String speed="";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        
        LocationManager mylocman = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	LocationListener myloclist = new MylocListener();
    	mylocman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,myloclist);
	}
	
	public class MylocListener implements LocationListener {
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
            String text = " My location is  Latitude ="+loc.getLatitude() + " Longitude =" + loc.getLongitude() + " Speed =" + loc.getSpeed();
            lat=loc.getLatitude() + "";
            log=loc.getLongitude()+"";
            speed=loc.getSpeed()+"";
            //updateDatabase();
		}
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
		public void onProviderEnabled (String provider) {
			// TODO Auto-generated method stub
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}
	
}
