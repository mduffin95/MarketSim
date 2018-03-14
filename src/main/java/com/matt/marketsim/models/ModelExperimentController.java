package com.matt.marketsim.models;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;

import java.util.concurrent.TimeUnit;

/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {


    /**
     * runs the model
     */
    public static void main(String[] args) {
        double VAR_SHOCK = 150000000;
        double VAR_PV = 100000000;
        double k = 0.05;
        double MEAN_FUNDAMENTAL = 100000;
        double ALPHA = 0.001; //Arbitrageur threshold
        double DELTA = 0.0;
        double OFFSET_RANGE = 2000;
        double LAMBDA = 0.075;
        int simLength = 15000;

        
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        MarketSimModel model = new TwoMarketModel(timeUnit, simLength, ALPHA, MEAN_FUNDAMENTAL, k, VAR_PV, VAR_SHOCK,
                OFFSET_RANGE, LAMBDA, DELTA);
//        model.setSeed(1);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(simLength, timeUnit);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }
}
