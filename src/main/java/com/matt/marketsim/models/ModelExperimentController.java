package com.matt.marketsim.models;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;

import java.util.concurrent.TimeUnit;

/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {
    public static String name;
    static final double VAR_SHOCK = 150000000;
    static final double VAR_PV = 100000000;
    static final double k = 0.05;
    static final double MEAN_FUNDAMENTAL = 100000;
    static final double ALPHA = 0.001; //Arbitrageur threshold
    static final double DELTA = 0;
    static final double OFFSET_RANGE = 2000;
    static final double LAMBDA = 0.075;
    static final int simLength = 15000;
    static final int ROUNDS = 1;

    public static void runOnce(long seed) {


        name = "results_" + String.valueOf((int)DELTA) + ".txt";


        TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        MarketSimModel model = new TwoMarketModel(timeUnit, simLength, ALPHA, MEAN_FUNDAMENTAL, k, VAR_PV, VAR_SHOCK,
                OFFSET_RANGE, LAMBDA, DELTA);
        model.setSeed(seed);
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

    /**
     * runs the model
     */
    public static void main(String[] args) {
        for (int i=0; i< ROUNDS; i++)
            runOnce(i);
    }
}
