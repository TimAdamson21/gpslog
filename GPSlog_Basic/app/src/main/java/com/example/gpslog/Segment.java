package com.example.gpslog;

import android.location.Location;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by timad on 7/7/2017.
 */

// A Segment contains the pertinent features of a set of tracks that have a hidden state
// of either ACCELERATION or STOPPED
public class Segment {

    // This will determine whether the tracks in the Segment are sent
    public boolean toSend;

    // These are the 5 features that will be passed to SVM
    private double stopsWRTime;
    private double stopsWRSpace;
    private double peakSpeedWRMaxSpeed;
    private double maxSingleStopWRTime;
    private double totStopTimeWRTime;

    // These are extra features for calculation and analysis purposes
    private double peakSpeed;
    private long maxSingleStop;
    private long totStopTime;
    private double avgAcc;
    private double avgSpeed;
    private int numStops;
    private long segTimeLength;
    private double distanceTraveled;
    private double maxSpeed;

    public Segment(){

        stopsWRTime = 0.0;
        stopsWRSpace = 0.0;
        peakSpeedWRMaxSpeed = 0.0;
        maxSingleStopWRTime = 0;
        totStopTimeWRTime = 0;
        peakSpeed = 0.0;
        maxSingleStop = 0;
        totStopTime = 0;
        avgAcc = 0.0;
        avgSpeed = 0.0;
        numStops = 0;
        segTimeLength = 0;
        distanceTraveled = 0.0;
        maxSpeed = 0.0;
    }

    // This constructor takes in a an ArrayList of Tracks and uses them to set the features of 
    // the Segment
    public Segment(ArrayList<Track> tracks){
        this();
        try {
            this.setFeatures(tracks);
        }
        catch (ParseException e){
            System.err.println("ParseException: " + e.getMessage());
        }
    }

    // Uses the ArrayList of Tracks passed in to set the features of the Segment
    // Precondition: All the tracks have been classified and given a hiddenState
    // Throws a ParseException if the SimpleDateFormat can not parse the time in the Tracks
    public void setFeatures(ArrayList<Track> tracks) throws ParseException {

        double currAcc = 0.0;
        double currSpd = 0.0;
        double accSum = 0.0;
        int beginStopIndex = 0;
        long currentStopTime;
        boolean isStopped = false;
        boolean hasStopped = false;
        Track currTrack = tracks.get(0);
        Track prevTrack;
        double speedsSum = currTrack.speed;
        maxSpeed = currTrack.speed;

        //Handling the case where the app is started standing still (stop first)
        if (currTrack.hiddenState == HMMClassifier.STOPPED) {
            isStopped = true;
            hasStopped = true;
            beginStopIndex = 0;
            numStops++;
        }

        //Analyzes a track and notates its features depending on the state of the car
        for (int i = 1; i < tracks.size(); i++) {
            prevTrack = currTrack;
            currTrack = tracks.get(i);
            if (currTrack.speed > maxSpeed) {
                maxSpeed = currTrack.speed;
            }
            //Car begins a stop
            if (!isStopped && tracks.get(i).hiddenState == HMMClassifier.STOPPED) {
                isStopped = true;
                hasStopped = false;
                currAcc = 0.0;
                currSpd = 0.0;
                numStops++;
                beginStopIndex = i;
            }
            //Car continues accelerating
            else if (!isStopped && currTrack.hiddenState == HMMClassifier.ACCELERATION) {
                currAcc = (currTrack.speed - prevTrack.speed) / (trackTimeDiff(currTrack, prevTrack));
                currSpd = currTrack.speed;
            }
            //Car begins accelerating
            else if (isStopped && currTrack.hiddenState == HMMClassifier.ACCELERATION) {
                isStopped = false;
                hasStopped = true;
                currAcc = (currTrack.speed - prevTrack.speed) / (trackTimeDiff(currTrack, prevTrack));
                currSpd = currTrack.speed;
                currentStopTime = trackTimeDiff(tracks.get(beginStopIndex), currTrack);
                totStopTime += currentStopTime;
                if (currentStopTime > maxSingleStop) {
                    maxSingleStop = currentStopTime;
                }
            }
            //Car continues a stop
            else if (isStopped && currTrack.hiddenState == HMMClassifier.STOPPED) {
                currAcc = 0.0;
                currSpd = 0.0;
            } else {
                Log.d("Error", "Car state not properly set");
            }

            //Note that acceleration for track[0] can't be calculated; it is not included in the avg
            accSum += currAcc;
            speedsSum += currSpd;
            if (hasStopped == true && peakSpeed < currSpd) {
                peakSpeed = currSpd;
            }
        }

        //Set helper features
        setPeakSpeed(peakSpeed);
        setMaxSingleStop(maxSingleStop);
        setTotStopTime(totStopTime);
        setAvgAcc(accSum / tracks.size());
        setAvgSpeed(speedsSum / tracks.size());
        setNumStops(numStops);
        setSegTimeLength(trackTimeDiff(tracks.get(0), tracks.get(tracks.size()-1)));
        setDistanceTraveled(trackDist(tracks.get(0), tracks.get(tracks.size()-1)));
        setMaxSpeed(maxSpeed);

        //Set SVM Features
        setStopsWRTime(numStops, segTimeLength);
        Log.i("Segment", "" + String.valueOf(stopsWRTime));
        setStopsWRSpace(numStops, distanceTraveled);
        setPeakSpeedWRMaxSpeed(peakSpeed, maxSpeed);
        setMaxSingleStopWRTime(maxSingleStop, segTimeLength);
        setTotStopTimeWRTime(totStopTime, segTimeLength);
    }

    // Returns the time passed between two tracks in milliseconds
    private long trackTimeDiff(Track startTrack, Track endTrack) throws ParseException {
        Date timeStart = df.parse(startTrack.time);
        Date timeEnd = df.parse(endTrack.time);
        return (timeEnd.getTime() - timeStart.getTime());
    }

    // Returns the distance traveled between two tracks in meters
    private double trackDist(Track startTrack, Track endTrack){
        Location startLoc = new Location("");
        startLoc.setLatitude(startTrack.latitude);
        startLoc.setLongitude(startTrack.longitude);
        Location lastLoc = new Location("");
        lastLoc.setLatitude(endTrack.latitude);
        lastLoc.setLongitude(endTrack.longitude);

        return lastLoc.distanceTo(startLoc);
    }

    // Returns the stops with respect to time of the Segment
    public double getStopsWRTime() {
        return stopsWRTime;
    }

    // Sets the stops WR time based on the values passed in
    public void setStopsWRTime(int stops, long time) {
        double stopsD = stops;
        if (time != 0 && stopsD != 0) {
            this.stopsWRTime = stopsD / time;
        }
        else {
            this.stopsWRTime = 0.0;
        }
    }

    // Returns the stops with respect to distance of the Segment
    public double getStopsWRSpace() {
        return stopsWRSpace;
    }

    // Sets the stops WR distance based on the values passed in
    public void setStopsWRSpace(int stops, double distance) {
        if (distance != 0 && stops != 0) {
            this.stopsWRSpace = stops / distance;
        }
        else {
            this.stopsWRSpace = 0.0;
        }
    }

    // Returns the Peak Speed WR to the Max Speed of the Segment
    public double getPeakSpeedWRMaxSpeed() {
        return peakSpeedWRMaxSpeed;
    }

    // Sets the Peak Speed WR to Max Speed based on the values passed in
    public void setPeakSpeedWRMaxSpeed(double peakSpeed, double maxSpeed) {
        if (peakSpeed != 0 && maxSpeed != 0) {
            this.peakSpeedWRMaxSpeed = peakSpeed / maxSpeed;
        }
        else {
            this.peakSpeedWRMaxSpeed = 0.0;
        }
    }

    // Returns the Max Single Stop Time WR to the length of the Segment
    public double getMaxSingleStopWRTime() {
        return maxSingleStopWRTime;
    }

    // Sets the Max Single Stop WR to Time based on the values passed in
    public void setMaxSingleStopWRTime(long maxSingleStop, long segTimeLength) {
        double maxSingleStopD = maxSingleStop;

        if (maxSingleStopD != 0 && segTimeLength != 0) {
            this.maxSingleStopWRTime = maxSingleStopD / segTimeLength;
        }
        else {
            this.maxSingleStopWRTime = 0.0;
        }
    }

    // Returns the Total Stopped Time WR to the Total Time
    public double getTotStopTimeWRTime() {
        return totStopTimeWRTime;
    }

    //Sets the total stopped time WR to time based on the values passed in
    public void setTotStopTimeWRTime(long totStopTime, long segTimeLength) {
        double totStopTimeD = totStopTime;
        if (totStopTimeD != 0 && segTimeLength != 0) {
            this.totStopTimeWRTime = totStopTimeD / segTimeLength;
        }
        else {
            this.totStopTimeWRTime = 0.0;
        }
    }

    // Returns the peak speed of the Segment
    public double getPeakSpeed() {
        return peakSpeed;
    }

    // Sets the peak speed of the segment to be the double passed in
    public void setPeakSpeed(double peakSpeed) {
        this.peakSpeed = peakSpeed;
    }

    // Returns the longest stop time of the Segment
    public double getMaxSingleStop() {
        return maxSingleStop;
    }

    // Sets the longest stop time of the Segment to be the long passed in
    public void setMaxSingleStop(long maxSingleStop) {
        this.maxSingleStop = maxSingleStop;
    }

    // Returns the total milliseconds spent at a stop for this Segment
    public double getTotStopTime() {
        return totStopTime;
    }

    // Sets the total time spent stopped for this Segment to be the long passed in
    public void setTotStopTime(long totStopTime) {
        this.totStopTime = totStopTime;
    }

    // Returns the average acceleration of the segment
    public double getAvgAcc() {
        return avgAcc;
    }

    // Sets the average acceleration to be the double passed in
    public void setAvgAcc(double avgAcc) {
        this.avgAcc = avgAcc;
    }

    // Returns the average velocity of the segment
    public double getAvgSpeed() {
        return avgSpeed;
    }

    // Sets the average velocity to be the double passed in
    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    // Returns the number of stops in this segment
    public int getNumStops() {
        return numStops;
    }

    // Sets the number of stops to be the int passed in
    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }

    // Returns the time length of the segment
    public long getSegTimeLength() {
        return segTimeLength;
    }

    // Sets the time length of the segment to be the long passed in
    public void setSegTimeLength(long segTimeLength) {
        this.segTimeLength = segTimeLength;
    }

    // Returns the distance traveled during the segment
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    // Sets the distance traveled during the segment to be the double passed in
    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    // Returns the max velocity of the segment
    public double getMaxSpeed() {
        return maxSpeed;
    }

    // Sets the max velocity of the segment to be the double passed in
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    //Format string is the format that dates will be passed in - returns long
    public final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
