package com.example.gpslog;

// Creates a Track object with the attributes found below.
// Each Track represents one piece of location data that will or will not
// be sent to the navigation server depending on the uniqueness of the information it contains
public class Track {
	String time;
	double latitude;
	double longitude;
	double speed;
	int hiddenState;
	String serial;
	int realTimeToSend;
}
