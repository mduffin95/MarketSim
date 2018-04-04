package com.matt.marketsim;

import com.matt.marketsim.models.ModelExperimentController;
import java.util.concurrent.Callable;

public class MarketSimCallable implements Callable {

    private long seed;
    private ModelParameters params;

    public MarketSimCallable(ModelParameters params) {
        this.params = params;
    }

    @Override
    public Object call() throws Exception {
        return ModelExperimentController.runOnce(params);
    }
}
