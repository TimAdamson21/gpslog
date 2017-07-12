package com.example.gpslog;

/**
 * Created by timad on 6/28/2017.
 */

import android.widget.Toast;

import java.util.ArrayList;

import libsvm.*;

public class SVMClassifier {

    svm_parameter param;
    svm_model model;
    DatabaseHandler dbh;

    public SVMClassifier(DatabaseHandler dbh){

        this.dbh = dbh;
        param = new svm_parameter();
        model = new svm_model();

        param.kernel_type = svm_parameter.POLY;
        //param.svm_type = null;
        param.degree = 10;
        //param.gamma = null;
        //param.coef0 = null;

        model.param = param;
        //model.nr_class = null;
        //model.l = null;
        model.SV = null;
        model.sv_coef = null;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.sv_indices = null;

    }

    public void classifyStopAndGo(){
        ArrayList<Segment> segments = getSegments();
    }

    private ArrayList<Segment> getSegments(){
        ArrayList<Track> tracks= (ArrayList<Track>)dbh.getAllTracks();
        ArrayList<Segment> segments = new ArrayList<>();
        boolean isSegment = false;
        ArrayList<Track> segTracks = new ArrayList<>();
        for(int i = 1; i < tracks.size(); i++){
            if(tracks.get(i - 1).hiddenState == HMMClassifier.FREEFLOW &&
                    tracks.get(i).hiddenState == HMMClassifier.ACCELERATION){
                isSegment = true;
            }
            else if(isSegment && tracks.get(i).hiddenState == HMMClassifier.FREEFLOW){
                isSegment = false;
                segments.add(new Segment(segTracks));
                segTracks.clear();

            }
            if(isSegment){
                segTracks.add(tracks.get(i));
            }

        }
        return segments;
    }

}
