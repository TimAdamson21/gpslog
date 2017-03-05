package com.example.gpslog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GPSDatabase {
	private Context context;
	private DbHelper dbHelper;
	public final String DBNAME="tracks";
	public final int DBVERSION=1;
	public SQLiteDatabase db;
	public final String COLUMN1="time";
	public final String COLUMN2="latitude";
	public final String COLUMN3="longitude";
	public final String COLUMN4="speed";

	public final String TABLENAME="location";
	public final String CREATERDB="CREATE TABLE tracks (" +
    		"time DATETIME," +
    		"latitude REAL," +
    		"longitude REAL," +
    		"speed REAL)";
    		
	//const
	public GPSDatabase(Context context){
		this.context=context;
		dbHelper=new DbHelper(context);
	}
	public class DbHelper extends SQLiteOpenHelper{
		public DbHelper(Context context){
			super(context,DBNAME,null,DBVERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(CREATERDB);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
	}
	public long insertRows(Double latitude, Double longitude, Double speed){
		ContentValues value=new ContentValues();
		value.put(COLUMN2, latitude);
		value.put(COLUMN3, longitude);
		value.put(COLUMN4, speed);
		return db.insert(TABLENAME,null,value);
	}
	public Cursor getAllRows(){
		Cursor cursor=db.query(TABLENAME, new String[]{COLUMN1,COLUMN2,COLUMN3, COLUMN4}, null,null, null, null, null);
		return cursor;
	}
	public void open() throws  SQLException{
		db=        dbHelper.getWritableDatabase();
		//return true;
	}
	public void close(){
		dbHelper.close();
		//return true;
	}
}
