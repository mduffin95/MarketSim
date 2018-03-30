package com.matt.marketsim;

import com.matt.marketsim.models.ModelExperimentController;
import desmoj.core.simulator.ParameterManager;

import java.util.concurrent.Callable;

public class MarketSimCallable implements Callable {

    private long seed;
    private ParameterManager params;

    public MarketSimCallable(long seed, ParameterManager parameterManager) {
        this.seed = seed;
        this.params = parameterManager;
    }

    @Override
    public Object call() throws Exception {
        return ModelExperimentController.runOnce(seed, params);
    }
}
