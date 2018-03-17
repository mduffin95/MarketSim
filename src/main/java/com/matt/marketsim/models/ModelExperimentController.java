package com.matt.marketsim.models;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {
    static final double VAR_SHOCK = 150000000;
    static final double VAR_PV = 100000000;
    static final double k = 0.05;
    static final double MEAN_FUNDAMENTAL = 100000;
    static final double ALPHA = 0.001; //Arbitrageur threshold
    static double DELTA = 0;
    static final double OFFSET_RANGE = 2000;
    static final double LAMBDA = 0.075;
    static final int simLength = 15000;
    static final int ROUNDS = 10;
    static final int MAX_DELTA = 1000;

    public static void runOnce(long seed) {


        final String name = "results/tmp/results.csv";
        final Path path = Paths.get(name);

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
        model.writeResultsToFile(path);
        exp.finish();
    }

    /**
     * runs the model
     */
    public static void main(String[] args) {
        for (int i=0; i<=MAX_DELTA; i+= 100) {
            DELTA = i;
            for (int j=0; j< ROUNDS; j++)
                runOnce(j);
        }

    }
}
