package com.matt.marketsim.models;

import com.matt.marketsim.MarketParams;
import com.matt.marketsim.MarketSimCallable;
import com.matt.marketsim.dtos.ResultDto;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.ParameterManager;
import desmoj.core.simulator.TimeInstant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;

/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {

//    static final double VAR_SHOCK = 150000000;
//    static final double VAR_PV = 100000000;
//    static final double k = 0.05;
//    static final double MEAN_FUNDAMENTAL = 100000;
//    static final double ALPHA = 0.001; //Arbitrageur threshold
//    static final double OFFSET_RANGE = 2000;
//    static final double LAMBDA = 0.075;
//    static final double DISCOUNT_RATE = 0.0006;
//    static final int SIM_LENGTH = 15000;
//    static final int SEED_OFFSET = 1234;
//    static final int ROUNDS = 200;
//    static final int DELTA_STEPS = 11;
//    static final int STEP = 100; //How much to increment delta by each time
//    static final int NUM_EXCHANGES = 2; //How much to increment delta by each time
//    static final int AGENTS_PER_EXCHANGE = 125; //Make sure this is even

    private static ParameterManager parameterManager;

    private static void initializeModelParameters() {
        parameterManager = new ParameterManager();
        parameterManager.initializeModelParameter(Double.class, "SIGMA_SHOCK", Math.sqrt(150000000.0));
        parameterManager.initializeModelParameter(Double.class, "SIGMA_PV", Math.sqrt(100000000.0));
        parameterManager.initializeModelParameter(Double.class, "K", 0.05);
        parameterManager.initializeModelParameter(Double.class, "MEAN_FUNDAMENTAL", 100000.0);
        parameterManager.initializeModelParameter(Double.class, "ALPHA", 0.001);
        parameterManager.initializeModelParameter(Double.class, "OFFSET_RANGE", 2000.0);
        parameterManager.initializeModelParameter(Double.class, "LAMBDA", 0.075);
        parameterManager.initializeModelParameter(Double.class, "DISCOUNT_RATE", 0.0006);
        parameterManager.initializeModelParameter(Integer.class, "NUM_EXCHANGES", 2);
        parameterManager.initializeModelParameter(Integer.class, "AGENTS_PER_EXCHANGE", 125);
    }

    private static void initializeExperimentParameters() {
        parameterManager.declareExperimentParameter(Integer.class, "DELTA_STEPS", 11);
        parameterManager.declareExperimentParameter(Integer.class, "STEP", 100);
        parameterManager.declareExperimentParameter(Integer.class, "ROUNDS", 200);
        parameterManager.declareExperimentParameter(Integer.class, "SIM_LENGTH", 15000);
        parameterManager.declareExperimentParameter(Integer.class, "SEED_OFFSET", 1234);
    }

    public static ResultDto runOnce(long seed, double delta) {

        TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
        exp.setReferenceUnit(timeUnit);
//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        parameterManager.initializeModelParameter(Double.class, "DELTA", delta);
        MarketSimModel model = new TwoMarketModel(parameterManager);
        model.setSeed(seed);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant((int)parameterManager.getParameterValue("SIM_LENGTH"), timeUnit);
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
        initializeModelParameters();
        initializeExperimentParameters();

        long start = System.currentTimeMillis();
        final String dir = "results/tmp/";
        double delta;
        int count = 0;
        int ROUNDS = (int)parameterManager.getParameterValue("ROUNDS");
        int DELTA_STEPS = (int)parameterManager.getParameterValue("DELTA_STEPS");
        int STEP = (int)parameterManager.getParameterValue("STEP");
        int SEED_OFFSET = (int)parameterManager.getParameterValue("SEED_OFFSET");

        List<ResultDto> allResults = new ArrayList<>(ROUNDS *
                (int)parameterManager.getParameterValue("DELTA_STEPS"));
        boolean parallel = true;
        if (parallel) {
            List<Callable<ResultDto>> tasks = new ArrayList<>();
            for (int i = 0; i < DELTA_STEPS; i++) {
                delta = i * STEP;
                for (int j = 0; j < ROUNDS; j++) {
                    count++;
                    System.out.println(count);
                    MarketSimCallable c = new MarketSimCallable(SEED_OFFSET + count, delta);
//                    MarketSimCallable c = new MarketSimCallable(SEED_OFFSET + count, delta);
                    tasks.add(c);
                }
            }

            ExecutorService EXEC = Executors.newFixedThreadPool(4);
            try {
                List<Future<ResultDto>> results = EXEC.invokeAll(tasks);
                for (Future<ResultDto> fr : results) {
                    allResults.add(fr.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                EXEC.shutdown();
            }

        } else {
            for (int i = 0; i < DELTA_STEPS; i++) {
                delta = i * STEP;
                for (int j = 0; j < ROUNDS; j++) {
                    count++;
                    ResultDto result = runOnce(SEED_OFFSET + count, delta);
                    allResults.add(result);
                    System.out.println(count);
                }
            }
        }
        System.out.println(System.currentTimeMillis() - start);
        writeToFile(dir, allResults);
    }

    private static void writeToFile(String dir, List<ResultDto> results) {
//        try {
//            FileUtils.cleanDirectory(new File(dir));
//        } catch (IOException e) {
//            System.err.println("Directory not found.");
//            return;
//        }

        for (ResultDto r : results) {
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
