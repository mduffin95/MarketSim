package com.matt.marketsim.models;

import com.matt.marketsim.MarketSimCallable;
import com.matt.marketsim.ModelParameters;
import com.matt.marketsim.dtos.ResultDto;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Parameter;
import desmoj.core.simulator.ParameterManager;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


/*
 * This class is used to initialise models and experiments. It is the entry point. It also allows multiple runs of
 * a model to allow results to be averaged.
 */
public class ModelExperimentController {

    private static void initializeModelParameters(String[] args, ModelParameters params) {

        params.addParameter(Double.class, "DELTA", 20.0);
        params.addParameter(Double.class, "SIGMA_SHOCK", Math.sqrt(150000000.0));
        params.addParameter(Double.class, "SIGMA_PV", Math.sqrt(100000000.0));
        params.addParameter(Double.class, "K", 0.05);
        params.addParameter(Double.class, "MEAN_FUNDAMENTAL", 100000.0);
        params.addParameter(Double.class, "ALPHA", 0.001);
        params.addParameter(Double.class, "OFFSET_RANGE", 2000.0);
        params.addParameter(Double.class, "LAMBDA", 0.075);
        params.addParameter(Double.class, "DISCOUNT_RATE", 0.0006);
        params.addParameter(Integer.class, "NUM_EXCHANGES", 2);
        params.addParameter(Integer.class, "AGENTS_PER_EXCHANGE", 125);
        params.addParameter(Integer.class, "SIM_LENGTH", 15000);
        params.addParameter(Boolean.class, "LA_PRESENT", true);

        params.addParameter(Integer.class, "DELTA_STEPS", 50);
        params.addParameter(Integer.class, "DELTA_OFFSET", 5);
        params.addParameter(Integer.class, "STEP", 1);
        params.addParameter(Integer.class, "ROUNDS", 1000);
        params.addParameter(Integer.class, "SEED_OFFSET", 1234);

        /* ZIP Experiment */
//        params.addParameter(Integer.class,"BUY_AGENTS_PER_EXCHANGE", 75);
//        params.addParameter(Integer.class,"SELL_AGENTS_PER_EXCHANGE", 75);
//
//        params.addParameter(Integer.class,"MIN_BUY_LIMIT", 70000);
//        params.addParameter(Integer.class, "MIN_SELL_LIMIT", 70000);
//        params.addParameter(Integer.class, "LIMIT_STEP", 1000);

        updateParams(args, params); //Do last to overwrite
    }

    public static ResultDto runOnce(ModelParameters params) {

        // create model and experiment
        Experiment.setEpsilon(TimeUnit.MICROSECONDS);
        Experiment.setReferenceUnit(TimeUnit.SECONDS);
        Experiment exp = new Experiment("Exp1");

        MarketSimModel model = new TwoMarketModel(params);

        long seed = (int)params.getParameter("SEED");
        model.setSeed(seed);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant((int)params.getParameter("SIM_LENGTH"));
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
//        exp.report();
        ResultDto result = model.getResults();
        exp.finish();
        return result;
    }

    private static void updateParams(String[] args, ModelParameters params) {
        Options options = new Options();
//        options.addOption("a", "arbitrageur", false, );
//        options.addOption("e", "num-exchanges", true, );
//        options.addOption("r", "rounds", true, "Number of rounds");

        Option arbOption = Option.builder("a")
                .longOpt("arbitrageur")
                .required(false)
                .hasArg(false)
                .desc("Presence of a latency arbitraguer")
                .build();

        Option exchangeOption = Option.builder("e")
                .longOpt("num-exchanges")
                .required(false)
                .hasArg(true)
                .desc("Number of exchanges")
                .build();

        Option agentsOption = Option.builder("t")
                .longOpt("agents-per-exchange")
                .required(false)
                .hasArg(true)
                .desc("Number of trading agents per exchange")
                .build();

        Option roundsOption = Option.builder("r")
                .longOpt("rounds")
                .required(false)
                .hasArg(true)
                .desc("Number of rounds")
                .build();

        options.addOption(arbOption);
        options.addOption(exchangeOption);
        options.addOption(agentsOption);
        options.addOption(roundsOption);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        if (cmd.hasOption("arbitrageur")) {
            System.out.println("LA_PRESENT == true");
            params.addParameter(Boolean.class,"LA_PRESENT", true);
        } else {
            System.out.println("LA_PRESENT == false");
            params.addParameter(Boolean.class, "LA_PRESENT", false);
        }

        String num_ex = cmd.getOptionValue("num-exchanges");
        if (num_ex != null) {
            System.out.println("NUM_EXCHANGES == " + num_ex);
            params.addParameter(Integer.class,"NUM_EXCHANGES", Integer.valueOf(num_ex));
        }

        String num_rounds = cmd.getOptionValue("rounds");
        if (num_rounds != null) {
            System.out.println("ROUNDS == " + num_rounds);
            params.addParameter(Integer.class,"ROUNDS", Integer.valueOf(num_rounds));
        }

        String num_agents = cmd.getOptionValue("agents-per-exchange");
        if (num_agents != null) {
            System.out.println("AGENTS_PER_EXCHANGE == " + num_agents);
            params.addParameter(Integer.class,"AGENTS_PER_EXCHANGE", Integer.valueOf(num_agents));
        }
    }

    /**
     * runs the model
     */
    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        final String dir = "results/runs/";
        double ind_var;
        int count = 0;
        ModelParameters params = new ModelParameters();
        initializeModelParameters(args, params);

        int ROUNDS = (int)params.getParameter("ROUNDS");
        int DELTA_STEPS = (int)params.getParameter("DELTA_STEPS");
        int STEP = (int)params.getParameter("STEP");
        int SEED_OFFSET = (int)params.getParameter("SEED_OFFSET");
        int DELTA_OFFSET = (int)params.getParameter("DELTA_OFFSET");

        List<ResultDto> allResults = new ArrayList<>(ROUNDS * DELTA_STEPS);
        boolean parallel = true;
        if (parallel) {
            List<Callable<ResultDto>> tasks = new ArrayList<>();
            for (int i = 0; i < DELTA_STEPS; i++) {
                ind_var = i * STEP + DELTA_OFFSET;
                for (int j = 0; j < ROUNDS; j++) {
                    count++;
                    System.out.println(SEED_OFFSET + count + ", " + ind_var);
                    ModelParameters p = new ModelParameters(params);
                    p.addParameter(Double.class, "LAMBDA", 1.0 / ind_var);
                    p.addParameter(Long.class, "SEED", SEED_OFFSET + count);
//                    all_params.add(p);
                    tasks.add(new MarketSimCallable(p));
                }
            }

            int processors = Runtime.getRuntime().availableProcessors();
            System.out.println("Num processors = " + processors);
            ExecutorService EXEC = Executors.newFixedThreadPool(processors);

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
                ind_var = i * STEP;
                for (int j = 0; j < ROUNDS; j++) {
                    count++;
                    ModelParameters p = new ModelParameters(params);
                    p.addParameter(Double.class, "LAMBDA", 1.0 / ind_var);
                    p.addParameter(Long.class, "SEED", SEED_OFFSET + count);
                    ResultDto result = runOnce(p);
                    allResults.add(result);
                    System.out.println(count);
                }
            }
        }
        System.out.println(System.currentTimeMillis() - start);
        writeToFile(params, dir, allResults);
    }

    private static void writeToFile(ModelParameters params, String dir, List<ResultDto> results) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy-MM-dd_HH:mm:ss");
        LocalDateTime date = LocalDateTime.now();
        dir = dir + "/" + dtf.format(date);

        try {
            Files.createDirectories(Paths.get(dir));
            File f = new File(dir, "experiment.txt");
            FileWriter writer = new FileWriter(f);
            BufferedWriter buf = new BufferedWriter(writer);

            buf.write(params.toString());
            buf.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (ResultDto r : results) {
            for (String[] ent : r.entries) {
                Path path = Paths.get(dir, ent[0] + ".csv");
                double ind_var = 1.0 / (double)r.params.getParameter("LAMBDA");
                ent[0] = String.valueOf(ind_var);
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
