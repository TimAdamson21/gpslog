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

public class Segment {

    private int numStops;
    private long segTimeLength;
    private double distanceTraveled;
    //TODO correctly implement peekSpeed
    private double peekSpeed; //Not correctly implemented
    private double maxSpeed;
    private long maxSingleStop;
    private long totStopTime;

    public Segment(){
        numStops = 0;
        segTimeLength = 0;
        distanceTraveled = 0.0;
        peekSpeed = 0.0;
        maxSpeed = 0.0;
        maxSingleStop = 0;
        totStopTime = 0;
    }

    public Segment(ArrayList<Track> tracks){
        this();
        try {
            setFeatures(tracks);
        }
        catch (ParseException e){
            System.err.println("ParseException: " + e.getMessage());
        }
    }


    public void setFeatures(ArrayList<Track> tracks) throws ParseException {

        setSegTimeLength(tracks);
        setDistanceTraveled(tracks);

        boolean stopped = false;
        boolean hasStopped = false;
        Date beginningStopTime = new Date();

        for (int i = 1; i < tracks.size(); i++) {
            Track currTrack = tracks.get(i);
            int currHiddenState = currTrack.hiddenState;
            int prevHiddenState = tracks.get(i - 1).hiddenState;

            if (currTrack.speed > maxSpeed) {
                maxSpeed = currTrack.speed;
            }

            if (currHiddenState == HMMClassifier.STOPPED &&
                    prevHiddenState == HMMClassifier.ACCELERATION) {
                numStops++;
                stopped = true;
                hasStopped = true;
                beginningStopTime = df.parse(currTrack.time);

            } else if (currHiddenState == HMMClassifier.ACCELERATION &&
                    prevHiddenState == HMMClassifier.STOPPED) {
                stopped = false;
                long stoppedTime = df.parse(currTrack.time).getTime() - beginningStopTime.getTime();
                totStopTime += stoppedTime;
                if(stoppedTime > maxSingleStop){
                    maxSingleStop = stoppedTime;
                }
            }

            if (hasStopped && currTrack.speed > peekSpeed) {
                peekSpeed = currTrack.speed;
            }

        }

    }

    public int getNumStops() {
        return numStops;
    }

    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }

    public long getSegTimeLength() {
        return segTimeLength;
    }

    public void setSegTimeLength(long segTimeLength) {
        this.segTimeLength = segTimeLength;
    }

    private void setSegTimeLength(ArrayList<Track> tracks) throws ParseException {
        Date time1 = new Date();
        Date time2 = new Date();
        time1 = df.parse(tracks.get(0).time);
        time2 = df.parse(tracks.get(tracks.size() - 1).time);
        segTimeLength = time2.getTime() - time1.getTime();
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

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

    public double getPeekSpeed() {
        return peekSpeed;
    }

    public void setPeekSpeed(double peekSpeed) {
        this.peekSpeed = peekSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMaxSingleStop() {
        return maxSingleStop;
    }

    public void setMaxSingleStop(long maxSingleStop) {
        this.maxSingleStop = maxSingleStop;
    }

    public double getTotStopTime() {
        return totStopTime;
    }

    public void setTotStopTime(long totStopTime) {
        this.totStopTime = totStopTime;
    }

    public final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
