package com.example.gpslog;

/**
 * Created by timad on 7/7/2017.
 */

public class Segment {

    private int numStops;
    private double segTimeLength;
    private double distanceTraveled;
    private double peekSpeed;
    private double maxSpeed;
    private double maxSingleStop;
    private double totStopTime;

    public int getNumStops() {
        return numStops;
    }

    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }

    public double getSegTimeLength() {
        return segTimeLength;
    }

    public void setSegTimeLength(double segTimeLength) {
        this.segTimeLength = segTimeLength;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
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

    public void setMaxSingleStop(double maxSingleStop) {
        this.maxSingleStop = maxSingleStop;
    }

    public double getTotStopTime() {
        return totStopTime;
    }

    public void setTotStopTime(double totStopTime) {
        this.totStopTime = totStopTime;
    }



}
