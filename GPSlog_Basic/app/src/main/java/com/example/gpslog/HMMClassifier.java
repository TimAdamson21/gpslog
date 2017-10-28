package com.example.gpslog;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.bluecraft.hmm.*;
import com.bluecraft.hmm.util.*;
import com.sun.xml.parser.*;
import com.ibm.xml.parser.*;
import com.sun.xml.tree.XmlDocumentBuilder;

import org.w3c.dom.*;

import java.net.URL;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.lang.*;

/**
 * Created by Nazib Siddique
 */


public class HMMClassifier implements Serializable, XmlParserList{

    Hmm hmm;


    // The HMMVlassifier constructor builds a model and creates an Hmm object from that model
    public HMMClassifier(){
        Model node = new Model(3,3, true);		//An hmm model named node is generated here
        hmm = new Hmm();
        // Set Model Parameters:

        // Set the Prior Vector pi
        node.setPi(0, 1); node.setPi(1, 0); node.setPi(2, 0);

        // Set the Transition Probabilities A
        node.setA(0, 0, 0.923076923); node.setA(0, 1, 0.019230763); node.setA(0, 2, 0.057692314);
        node.setA(1, 0, 1.15E-35); node.setA(1, 1, 0.983050844); node.setA(1, 2, 0.016949156);
        node.setA(2, 0, 0.040200996); node.setA(2, 1, 0.030150754); node.setA(2, 2, 0.92964825);

        // Set the Emission Probabilities B
        node.setB(0, 0, 1); node.setB(0, 1, 0); node.setB(0, 2, 0);
        node.setB(1, 0, 0); node.setB(1, 1, 1); node.setB(1, 2, 0);
        node.setB(2, 0, 0); node.setB(2, 1, 0); node.setB(2, 2, 1);

        hmm.setModel(node);

        System.out.println("Number of states: " + hmm.getNumStates());
        int[] modelSeq = hmm.generateSeq();
        for(int i = 0; i < modelSeq.length; i ++){
            System.out.println(modelSeq[i]);
        }

    }

    // Create a Sequence of dcrtspd to feed the HMM
    private int[] getDiscrtSpeedArray(DatabaseHandler db) {
        ArrayList<Track> tracks = (ArrayList<Track>) db.getAllTracks();
        int[] discrtSpeedArray = new int[tracks.size()];
        for (int i = 0; i < tracks.size(); i++) {
            discrtSpeedArray[i] = getDiscrtSpeed((float)tracks.get(i).speed);
        }
        return discrtSpeedArray;
    }

    public void setHmm(DatabaseHandler db){
        hmm.setObSeq(getDiscrtSpeedArray(db));
    }

    public int getHiddenState(float speed){
        int[] speedArray = new int[1];

        int DiscritizedSpeed = getDiscrtSpeed(speed);
        speedArray[0] = DiscritizedSpeed;
        hmm.setObSeq(speedArray);
        int[] maxLikelyState = hmm.getMaxLikelyState();
        return maxLikelyState[0];
    }

    public void addMaxLikelyStateToDataBase(DatabaseHandler db){
        int[] hmmresult = hmm.getMaxLikelyState();	// Get the Most Likely state from the observation sequence

    }

    private int getDiscrtSpeed(float speed) {
        int dcrtspd = 0;

        if (speed <= 1.34112) {    //  3 mph = 1.34112 m/s
            dcrtspd = 0;
        } else if (speed >= 8.9408) {        // 20 mph = 8.9408 m/s
            dcrtspd = 2;
        } else {
            dcrtspd = 1;
        }
        return dcrtspd;
    }

    public static final int ACCELERATION = 1;
    public static final int FREEFLOW = 2;
    public static final int STOPPED = 0;
    //TODO find out if this can be made into enum

}
