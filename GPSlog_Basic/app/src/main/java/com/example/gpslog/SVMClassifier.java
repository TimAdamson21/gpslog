package com.example.gpslog;

/**
 * Created by timad on 6/28/2017.
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import libsvm.*;


// This class is used to classify stop and go segments using the svm algorithm
public class SVMClassifier {

    DatabaseHandler dbh;
    Context myContext;
    svm_model model;
    BufferedReader br;

    // Constructor sets dbh and context for asset use from what is passed into the SVMClassifier
    public SVMClassifier(DatabaseHandler dbh, Context context){
        this.dbh = dbh;
        this.myContext = context;
    }

    // Runs the classifier and assigns a value to each track in the database as 0=notstopngo 1=stopngo
    public void classifyStopAndGo() {

        // Load model file as a buffered reader then pass it to svm_load: Runtime error will occur on svm_predict if either exception occurs
        try {
            AssetManager assetManager = myContext.getAssets();
            br = new BufferedReader(new InputStreamReader(assetManager.open("trainedmodel.model"), "UTF-8"));
            this.model = svm.svm_load_model(br);
        } catch(FileNotFoundException ex) {
            ex.printStackTrace();
            Log.e("Model Asset Error", "FileNotFound");
        } catch(IOException ex) {
            ex.printStackTrace();
            Log.e("Model Asset Error", "IOException");
        }

        // Collect segments from database handler
        ArrayList<Segment> segments = getSegments();

        /** Create an array of node Arrays called allSegmentNodes, to be passed to svm_predict.
        Each segmentNodes array represents the characteristic features of a segment.
        This process is necessary to set up the data such that it can be read by the svm library **/
        svm_node[][] allSegmentNodes = new svm_node[segments.size()][5];
        for (int i = 0; i < segments.size(); i++) {
            svm_node[] segmentNodes = createSegmentNodeArray(segments.get(i));
            allSegmentNodes[i] = segmentNodes;
        }

        // Predict each node in allSegmentNodes and store the prediction in predictedValues array
        double[] predictedValues = new double[segments.size()]; //:TODO Put this in a separate thread to improve performance?
        for (int i = 0; i < allSegmentNodes.length; i++) {
            predictedValues[i] = svm.svm_predict(model, allSegmentNodes[i]);
        }

        // Alter the database to reflect which tracks are stop and go
        updateDatabase(predictedValues, this.dbh);
    }

    /** Returns an array of Segments from the database, which are distinct parts of the trip
    The Track's hidden state is either ACCELERATION or STOPPED
    Each Segment will have characteristic features that can be used by SVM **/
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

    // Returns an svm node from index/value passed
    private svm_node createSvmNode(int index, double value) {
        svm_node node = new svm_node();
        node.index = index;
        node.value = value;
        return node;
    };

    // Creates an array of nodes that represent the soft-normalized values of the segment passed in
    private svm_node[] createSegmentNodeArray(Segment segment) {
        svm_node[] nodeArray = new svm_node[5];
        nodeArray[0] = createSvmNode(1, segment.getStopsWRTime());
        nodeArray[1] = createSvmNode(2, segment.getStopsWRSpace());
        nodeArray[2] = createSvmNode(3, segment.getPeakSpeedWRMaxSpeed());
        nodeArray[3] = createSvmNode(4, segment.getMaxSingleStopWRTime());
        nodeArray[4] = createSvmNode(5, segment.getTotStopTimeWRTime());
        return nodeArray;
    }

    private void updateDatabase(double[] predictions, DatabaseHandler dbh) {
        System.out.println(Arrays.toString(predictions));
        //TODO: Complete this method
    }
}
