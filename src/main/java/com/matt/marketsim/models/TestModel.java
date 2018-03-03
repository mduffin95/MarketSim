package com.matt.marketsim.models;

import com.matt.marketsim.builders.NetworkBuilder;
import com.matt.marketsim.builders.ZIPExperiment;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.concurrent.TimeUnit;

public class TestModel extends MarketSimModel {
    int arrival = 0;

    public TestModel(NetworkBuilder builder) {
        super(builder);
        setSeed(1);
    }

    @Override
    public TimeSpan getAgentArrivalTime() {
        TimeSpan t = new TimeSpan(arrival, TimeUnit.SECONDS);
        arrival++;
        return t;
    }

    public static void main(String[] args) {
        // create model and experiment
        Experiment exp = new Experiment("Exp1");
        NetworkBuilder builder = new ZIPExperiment(25, 0, 200);
        TestModel model = new TestModel(builder);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(MarketSimModel.SIM_LENGTH, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }

}