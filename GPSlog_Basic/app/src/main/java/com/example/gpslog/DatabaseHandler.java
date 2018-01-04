package com.example.gpslog;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.*;
import java.lang.Integer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DatabaseHandler extends SQLiteOpenHelper {

	public DatabaseHandler(Context context) {
		super(context, "tracks.db", null, 1);
	}


	@Override
    // Creates an SQLite "tracks" table with the names and types found in the String literal.
	public void onCreate(SQLiteDatabase db) {
        String CREATE_TRACKS_TABLE = "CREATE TABLE tracks (" +
        		"time TEXT ," +
        		"latitude TEXT," +
        		"longitude TEXT," +
        		"speed REAL,"+
        		"updateStatus TEXT," +
        		"serial TEXT," +
                "hiddenState REAL," +
                "toSend REAL," +
                "TripID INTEGER" +
                ")";
        db.execSQL(CREATE_TRACKS_TABLE);
		
	}

    // Creates a "tracks" table with the standard database
    // The table created will have the names and types found in the onCreate String literal
    public void OnCreateWithStandardDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }



	@Override
    // Drops the existing "tracks" table if there is one, and then creates a new "tracks" table
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tracks");
        onCreate(db);
	}

    // Drops the "tracks" table of the standard database if there is one, and adds a new "tracks" table
    public void standardOnUpgrade() {
        SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db, 0, 0);
    }

    // Accepts as parameters the name of the table to add a column to, and
    // the new column's name and type
    // Adds the column to the standard database
    public void addColumn(String tableName, String colName, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        String ADD_COLUMN = "ALTER TABLE " + tableName + " ADD " +colName + " " + type;
        db.execSQL(ADD_COLUMN);
    }

    // Adds an entry to the standard database "tracks" table with the column values set to
    // be the parameters passed in. The updateStatus will initially be set to "no" because the
    // database has yet to be synced with the server
	public void insertRow(String time, Double latitude, Double longitude,
                          Float speed, String serial, int state, int toSend, long tripID) {
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("speed", speed);
        values.put("updateStatus", "no");
        values.put("serial", serial);
        values.put("hiddenState", state);
        values.put("toSend", toSend);
        values.put("TripID", tripID);
        
        db.insert("tracks", null, values);
        db.close(); 
	}

    // Returns the number of rows contained in the "tracks" table
    // Returns -1 if there is no tracks table
    public long getTracksNumRows(){
        SQLiteDatabase db = this.getWritableDatabase();
        String numRowsQuery = "SELECT COUNT(*) FROM tracks";
        Cursor cursor = db.rawQuery(numRowsQuery, null);
        if(cursor.moveToFirst()){
            return cursor.getLong(0);
        }
        else{
            return -1;
        }
    }

    // Sets the "toSend" value of the track with the specified time to toSend integer passed in
    public void setToSend(String time, int toSend){
        SQLiteDatabase db = this.getWritableDatabase();
        String CHANGE_TIME = "UPDATE tracks" +
                " SET toSend = " + toSend +
                " WHERE time = '" + time +"';";
        db.execSQL(CHANGE_TIME);
    }

    //Selects the largest trip id in the data base, in order to augment by one
    //If all the table ids are null, this method will return 0
    public int getLastTripID(){
        int lastTrip = 0;
        String selectQuery = "SELECT TripID FROM tracks ORDER BY TripID DESC LIMIT 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            String largestTripId = cursor.getString(0);
            if (largestTripId != null) {
                lastTrip = Integer.parseInt(largestTripId);
            }
        }
        else {
            System.out.print("The table is empty, but that should be fine");
        }
        return  lastTrip;
    }

    // Returns a list of TripIDs
    //TODO finish implementing this method so that we can display the various trips on the map
    public List<String> getTripIDs(){
        List<String> ret = new ArrayList<String>();
        return ret;
    }

    // Returns a List of all the tracks in the standard database "tracks" table
	public List<Track> getAllTracks() {
	    List<Track> trackList = new ArrayList<Track>();
        String selectQuery = "SELECT latitude, longitude, speed, time, hiddenState, toSend FROM tracks";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Track track = new Track();
                track.time = cursor.getString(3);
                track.latitude = Double.parseDouble(cursor.getString(0));
                track.longitude = Double.parseDouble(cursor.getString(1));
                track.speed = Float.parseFloat(cursor.getString(2));
                track.hiddenState = Integer.parseInt(cursor.getString(4));
                track.realTimeToSend = Integer.parseInt(cursor.getString(5));
               // track.serial = cursor.getString(4);
                trackList.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return trackList;
    }


     // Returns an ArrayList of Cursors that contain the results of executing the Query on
     // the standard database, and any error messages that result from the Query execution
	public ArrayList<Cursor> getData(String Query){
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[] { "mesage" };
		//an array list of cursor to save two cursors one has results from the query 
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2= new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);
		
		
		try{
			String maxQuery = Query ;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);
			

			//add value to cursor2
			Cursor2.addRow(new Object[] { "Success" });
			
			alc.set(1,Cursor2);
			if (null != c && c.getCount() > 0) {

				
				alc.set(0,c);
				c.moveToFirst();
				
				return alc ;
			}
			return alc;
		} catch(SQLException sqlEx){
			Log.d("printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		} catch(Exception ex){

			Log.d("printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+ex.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		}

		
	}
	
    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        
        String selectQuery = "SELECT  * FROM tracks where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("time", cursor.getString(0));
				map.put("latitude", cursor.getString(1));
				map.put("longitude", cursor.getString(2));
				map.put("speed", cursor.getString(3));
				map.put("serial", cursor.getString(5));
                map.put("hidden_state", cursor.getString(6));
                map.put("toSend", cursor.getString(7));
                map.put("TripID", cursor.getString(8));
				
				//System.out.print( "inside JSON file first data " + cursor.getString(0) + " Second data " + cursor.getString(1) + " third data " + cursor.getString(2) + " fourth data " + cursor.getString(3)+ " fifth data" + cursor.getString(4));
                trackList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(trackList);
    }
    

    // Returns the number of tracks that are yet to be synced
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM tracks where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    } //TODO Improve method to use SELECT COUNT(*)
    
    /**
     * Update the sync status of the track that has the the time passed in to be the status String passed in
     */
    public void updateSyncStatus(String time, String status){
        SQLiteDatabase database = this.getWritableDatabase();     
        String updateQuery = "Update tracks set updateStatus = '"+ status +"' where time="+"'"+ time +"'";
        Log.d("query",updateQuery);        
        database.execSQL(updateQuery);
        database.close();
    }

    /** NEW <==
     * Get SQLite records that are yet to be Tagged
     * @return

    public int dbTagCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM tracks where Tag = '"+"no"+"'";
        //TODO create a Tag column in tracks.db
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }
    // Set the intial timestamp
    public String StartTime(){
        String starttime = 0;
        String selectQuery = "SELECT  time FROM tracks where Tag = '\"+\"no\"+\"'\" ORDER BY time ASC LIMIT 1";
        SQLiteDatabase database = this.getWritableDatabase();
        starttime = database.rawQuery(selectQuery, null);	//Get the time as String

        //Convert String to DateTime
        //String str_date="13-09-2011";
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = (Date)formatter.parse(starttime);
        //System.out.println("Today is " +date.getTime());

        database.close();
        return date.getTime();
    }*/
}