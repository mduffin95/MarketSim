package com.matt.marketsim;

import com.matt.marketsim.models.ModelExperimentController;

import java.util.concurrent.Callable;

public class MarketSimCallable implements Callable {

    private long seed;
    private double delta;

    public MarketSimCallable(long seed, double delta) {
        this.seed = seed;
        this.delta = delta;
    }

    @Override
    public Object call() throws Exception {
        return ModelExperimentController.runOnce(seed, delta);
    }
}
