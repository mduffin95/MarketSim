package com.matt.marketsim.models;

import com.google.gson.Gson;
import com.matt.marketsim.MarketSimCallable;
import com.matt.marketsim.dtos.ResultDto;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Parameter;
import desmoj.core.simulator.ParameterManager;
import desmoj.core.simulator.TimeInstant;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;


/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {

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
        parameterManager.initializeModelParameter(Integer.class, "DELTA_STEPS", 21);
        parameterManager.initializeModelParameter(Integer.class, "STEP", 1);
        parameterManager.initializeModelParameter(Integer.class, "ROUNDS", 200);
        parameterManager.initializeModelParameter(Integer.class, "SIM_LENGTH", 15000);
        parameterManager.initializeModelParameter(Integer.class, "SEED_OFFSET", 1234);
        parameterManager.initializeModelParameter(Boolean.class, "LA_PRESENT", false);
    }

    public static ResultDto runOnce(long seed, double delta) {

        // create model and experiment
        Experiment.setEpsilon(TimeUnit.MICROSECONDS);
        Experiment.setReferenceUnit(TimeUnit.SECONDS);
        Experiment exp = new Experiment("Exp1");

//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        parameterManager.initializeModelParameter(Double.class, "DELTA", delta);
        MarketSimModel model = new TwoMarketModel(parameterManager);
        model.setSeed(seed);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant((int)parameterManager.getParameterValue("SIM_LENGTH"));
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

        long start = System.currentTimeMillis();
        final String dir = "results/runs/";
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd_HH:mm:ss");
        LocalDateTime date = LocalDateTime.now();
        dir = dir + "/" + dtf.format(date);

        try {
            Files.createDirectories(Paths.get(dir));
            File f = new File(dir, "experiment.txt");
            FileWriter writer = new FileWriter(f);
            BufferedWriter buf = new BufferedWriter(writer);

            Collection<Parameter> params = parameterManager.getParameters();
            for (Parameter p: params) {
                buf.write(p.toString());
                buf.newLine();
            }
            buf.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
