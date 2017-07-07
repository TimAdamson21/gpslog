package com.example.gpslog;

/**
 * Created by timad on 6/28/2017.
 */

import android.widget.Toast;

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
        param.svm_type = null;
        param.degree = 10;
        param.gamma = null;
        param.coef0 = null;

        model.param = param;
        model.nr_class = null;
        model.l = null;
        model.SV = null;
        model.sv_coef = null;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.sv_indices = null;

    }

    public void classifyStopAndGo(){

        System.out.println("Soooo classified");
    }

}
