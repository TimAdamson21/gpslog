package com.example.gpslog;

/**
 * Created by timad on 6/28/2017.
 */

import android.widget.Toast;

import java.util.ArrayList;
import libsvm.*;

// This class is used to classify stop and go segments using the svm algorithm
public class SVMClassifier {

    svm_parameter param;
    svm_model model;
    DatabaseHandler dbh;

    // The constructor sets the model, and the svm parameters, as well as the databasehandler passed in
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

    // Marks the Tracks that are stop and go as being "stop and go" in the data table
    public void classifyStopAndGo(){
        ArrayList<Segment> segments = getSegments();
    }

    // Returns an array of Segments, which are distinct parts of the trip
    // where the Track's hidden state is either ACCELERATION or STOPPED
    // Each Segment will have characteristic features that can be used by SVM
    private ArrayList<Segment> getSegments(){
        ArrayList<Track> tracks= (ArrayList<Track>)dbh.getAllTracks();
        ArrayList<Segment> segments = new ArrayList<>();
        boolean isSegment = false;
        ArrayList<Track> segTracks = new ArrayList<>();
        for(int i = 1; i < tracks.size(); i++){
            if((tracks.get(i - 1).hiddenState == HMMClassifier.FREEFLOW &&
                    tracks.get(i).hiddenState == HMMClassifier.ACCELERATION) ||i == 1){
                isSegment = true;
            }
            else if((isSegment && tracks.get(i).hiddenState == HMMClassifier.FREEFLOW) ||
                    i == tracks.size() -1 ){
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
