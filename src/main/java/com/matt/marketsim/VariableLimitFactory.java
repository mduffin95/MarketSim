package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.TimeInstant;

public class VariableLimitFactory {
    private MarketSimModel model;
    private double k;
    private double fundamental;
    private ContDistNormal normal_shock;
    private ContDistNormal normal_pv;
    private double fundamental_mean;
//    private int n;
    private TimeInstant lastTime = null;

    public VariableLimitFactory(MarketSimModel model, double sigma_shock, double sigma_pv, double k, double initial_fundamental) {
        assert (k >= 0 && k <= 1);
        this.model = model;
        this.k = k;
        this.model = model;
        this.normal_shock = new ContDistNormal(this.model, "Shock", 0, sigma_shock, true, false);
        this.normal_pv = new ContDistNormal(this.model, "Price valuation", 0, sigma_pv, true, false);
        model.distributionManager.register(this.normal_shock);
        model.distributionManager.register(this.normal_pv);

//        this.n = 1;
        this.fundamental_mean = initial_fundamental;
        this.fundamental = initial_fundamental;
    }

    public double getFundamental() {
        TimeInstant currentTime = model.getExperiment().getSimClock().getTime();
        if (null == lastTime) {
            fundamentalIteration();
            lastTime = new TimeInstant(0);
        } else {
            int lt = (int)Math.floor(lastTime.getTimeAsDouble()); //Last time (in reference time unit)
            int ct = (int)Math.floor(currentTime.getTimeAsDouble()); //Current time
            int diff = ct - lt;
            for (int i = 0; i < diff; i++) {
                fundamentalIteration();
            }
            lastTime = currentTime;
        }

        return fundamental;
    }

    private void fundamentalIteration() {
        double shock = normal_shock.sample();
        fundamental = Math.max(0, k * fundamental_mean + (1 - k) * fundamental + shock);
//        n++;
//        fundamental_mean = fundamental_mean + (fundamental - fundamental_mean) / n;
    }

    public VariableLimit create() {
        return new VariableLimit(this, model.getExperiment().getSimClock(), normal_pv);
    }
}
