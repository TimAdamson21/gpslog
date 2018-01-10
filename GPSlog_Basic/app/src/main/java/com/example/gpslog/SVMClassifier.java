package com.example.gpslog;

/**
 * Created by timad on 6/28/2017.
 */

import android.util.Log;
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

        param.kernel_type = svm_parameter.POLY; //Polynomial kernel: (gamma*u'*v + coef0)^degree
        param.svm_type = 0; //C-SVC multi-class classification
        param.degree = 3; //Pre-Trained params with 90% accuracy on NYC data
        param.gamma = 0.8;
        param.coef0 = 10;

        model.param = param; //Params from above
        model.nr_class = 2; // Number of classes
        model.l = 69; // Number of Support Vectors
        model.rho = new double[]{-0.174707}; // Constants in decision functions
        model.label = new int[]{0, 1}; // 0=stopngo, 1=notstopngo
        model.nSV = new int[]{34, 35}; // Number of each category in the training data
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
        ArrayList<Track> segTracks = new ArrayList<>();
        boolean isSegment = false;

        //Makes a decision based on whether a segment is active, and if the current track is FreeFlow
        for(int i = 0; i < tracks.size(); i++){
            if (isSegment && (tracks.get(i).hiddenState == HMMClassifier.FREEFLOW || i == tracks.size()-1)) {
                isSegment = false;
                segments.add(new Segment(segTracks));
                segTracks.clear();
            }
            else if (isSegment && tracks.get(i).hiddenState != HMMClassifier.FREEFLOW) {
                segTracks.add(tracks.get(i));
            }
            else if (!isSegment && tracks.get(i).hiddenState != HMMClassifier.FREEFLOW) {
                isSegment = true;
                segTracks.add(tracks.get(i));
            }
        }
        return segments;
    }

}
