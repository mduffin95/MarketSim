package com.matt.marketsim.models;

import com.matt.marketsim.dtos.ResultDto;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

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
    static final int SIM_LENGTH = 15000;
    static final int SEED_OFFSET = 4;
    static final int ROUNDS = 1;
    static final int MAX_DELTA = 1;
    static final int STEP = 100; //How much to increment delta by each time
    static final int NUM_AGENTS = 250; //Make sure this is even

    public static ResultDto runOnce(long seed, double delta) {




        TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
        exp.setReferenceUnit(timeUnit);
//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        MarketSimModel model = new TwoMarketModel(SIM_LENGTH, NUM_AGENTS, ALPHA, MEAN_FUNDAMENTAL, k, VAR_PV, VAR_SHOCK,
                OFFSET_RANGE, LAMBDA, delta);
        model.setSeed(seed);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(SIM_LENGTH, timeUnit);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        ResultDto result = model.getResults();
        exp.finish();
        return result;
    }

    /**
     * runs the model
     */
    public static void main(String[] args) {
        final String dir = "results/tmp/";

        List<ResultDto> allResults = new ArrayList<ResultDto>(ROUNDS * (MAX_DELTA / STEP));
        for (int i=0; i<=MAX_DELTA; i+= STEP) {
            DELTA = i;
            for (int j=0; j< ROUNDS; j++) {
                ResultDto result = runOnce(SEED_OFFSET + j, DELTA);
                allResults.add(result);
            }
        }
        writeToFile(dir, allResults);
    }

    private static void writeToFile(String dir, List<ResultDto> results) {
        try {
            FileUtils.cleanDirectory(new File(dir));
        } catch (IOException e) {
            System.err.println("Directory not found.");
            return;
        }

        for (ResultDto r: results) {
            for (String[] ent : r.entries) {
                Path path = Paths.get(dir, ent[0] + ".csv");
                ent[0] = String.valueOf(r.delta);
                String toWrite = String.join(", ", ent);

                try {
                    Files.write(path, Arrays.asList(toWrite), Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
                } catch (IOException e) {
                    System.err.println("Error writing to file.");
                    return;
                }

            }
        }
    }
}
