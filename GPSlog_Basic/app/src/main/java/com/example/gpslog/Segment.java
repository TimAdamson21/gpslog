package com.example.gpslog;

import android.location.Location;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by timad on 7/7/2017.
 */

// A Segment contains the pertenant features of a set of tracks that have a hidden state 
// of either ACCELERATION or STOPPED
public class Segment {

    private int numStops;
    private long segTimeLength;
    private double distanceTraveled;
    //TODO correctly implement peakSpeed
    private double peakSpeed; //Not correctly implemented
    private double maxSpeed;
    private long maxSingleStop;
    private long totStopTime;

    // Creates a generic Segment with all the attributes set to 0
    public Segment(){
        numStops = 0;
        segTimeLength = 0;
        distanceTraveled = 0.0;
        peakSpeed = 0.0;
        maxSpeed = 0.0;
        maxSingleStop = 0;
        totStopTime = 0;
    }

    // This constructor takes in a an ArrayList of Tracks and uses them to set the features of 
    // the Segment
    public Segment(ArrayList<Track> tracks){
        this();
        try {
            setFeatures(tracks);
        }
        catch (ParseException e){
            System.err.println("ParseException: " + e.getMessage());
        }
    }

    // Uses the ArrayList of Tracks passed in to set the features of the Segment
    // Precondition: All the tracks have been classified and given a hiddenState
    // Throws a ParseException if the SimpleDateFormat can not parse the time in the Tracks
    public void setFeatures(ArrayList<Track> tracks) throws ParseException {

        setSegTimeLength(tracks);
        setDistanceTraveled(tracks);

        boolean isStopped = false;
        boolean hasStopped = false;
        double maxPeek = 0.0;
        Date beginningStopTime = new Date();

        // TODO: Make sure that this loop sets everything as required
        for (int i = 1; i < tracks.size(); i++) {
            Track currTrack = tracks.get(i);
            int currHiddenState = currTrack.hiddenState;
            int prevHiddenState = tracks.get(i - 1).hiddenState;

            if (currTrack.speed > maxSpeed) {
                maxSpeed = currTrack.speed;
            }

            if(currHiddenState == HMMClassifier.STOPPED){
                peakSpeed = maxPeek;
            }

            if (currHiddenState == HMMClassifier.STOPPED &&
                    prevHiddenState == HMMClassifier.ACCELERATION) {
                numStops++;
                isStopped = true;
                hasStopped = true;
                beginningStopTime = df.parse(currTrack.time);

            } else if (currHiddenState == HMMClassifier.ACCELERATION &&
                    prevHiddenState == HMMClassifier.STOPPED) {
                isStopped = false;
                long stoppedTime = df.parse(currTrack.time).getTime() - beginningStopTime.getTime();
                totStopTime += stoppedTime;
                if(stoppedTime > maxSingleStop){
                    maxSingleStop = stoppedTime;
                }
            }

            if (hasStopped && currHiddenState == HMMClassifier.ACCELERATION) {
                if(currTrack.speed > maxSpeed){
                    maxSpeed = currTrack.speed;
                }
            }

        }

    }

    // Returns the number of stops made
    public int getNumStops() {
        return numStops;
    }

    // Sets teh number of stops made
    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }

    // Returns the time length of the Segment
    public long getSegTimeLength() {
        return segTimeLength;
    }

    // Sets the time length of the Segment to be the long passed in
    public void setSegTimeLength(long segTimeLength) {
        this.segTimeLength = segTimeLength;
    }

    // Sets the time length of the Segment from the ArrayList of tracks passed in
    private void setSegTimeLength(ArrayList<Track> tracks) throws ParseException {
        Date time1 = new Date();
        Date time2 = new Date();
        time1 = df.parse(tracks.get(0).time);
        time2 = df.parse(tracks.get(tracks.size() - 1).time);
        segTimeLength = time2.getTime() - time1.getTime();
    }

    // Returns the distance traveled in meters
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    // Sets the distance traveled to be the double passed in
    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    // Sets the distance traveled from the ArrayList of tracks passed in
    private void setDistanceTraveled(ArrayList<Track> tracks){
        Location startLoc = new Location("");
        Track firstTrack = tracks.get(0);
        startLoc.setLatitude(firstTrack.latitude);
        startLoc.setLongitude(firstTrack.longitude);
        Location lastLoc = new Location("");
        Track lastTrack = tracks.get(tracks.size() - 1);
        lastLoc.setLatitude(lastTrack.latitude);
        lastLoc.setLongitude(lastTrack.longitude);

        distanceTraveled = lastLoc.distanceTo(startLoc);
    }

    // Returns the peak speed of the Segment
    public double getPeekSpeed() {
        return peakSpeed;
    }

    // Sets the peak speed of the segment to be the double passed in
    public void setPeekSpeed(double peakSpeed) {
        this.peakSpeed = peakSpeed;
    }

    // Returns the max speed of the Segment
    public double getMaxSpeed() {
        return maxSpeed;
    }

    // Sets the max speed of the Segment to be the double passed in
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    // Returns the longest stop time of the Segment
    public double getMaxSingleStop() {
        return maxSingleStop;
    }

    // Sets the longest stop time of the Segment to be the long passed in
    public void setMaxSingleStop(long maxSingleStop) {
        this.maxSingleStop = maxSingleStop;
    }

    // Returns the total number of stops for this Segment
    public double getTotStopTime() {
        return totStopTime;
    }

    // Sets the total number of stops for this Segment to be the long passed in
    public void setTotStopTime(long totStopTime) {
        this.totStopTime = totStopTime;
    }

    public final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
