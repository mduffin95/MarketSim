package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.TimeInstant;

public class VariableLimit {
    private double k;
    private double fundamental_mean;
    private int n;
    private double fundamental;
    private double sigma_shock;
    private double sigma_pv;
    private ContDistNormal normal_shock;
    private ContDistNormal normal_pv;
    private MarketSimModel model;
    private TimeInstant lastTime = null;

    //Member variables
    private int price_valuation;

    public VariableLimit(MarketSimModel model, double k, double initial_fundamental, ContDistNormal normal_shock, ContDistNormal normal_pv) {
        assert (k >= 0 && k <= 1);
        this.n = 1;
        this.fundamental_mean = initial_fundamental;
        this.fundamental = initial_fundamental;
        this.k = k;
        this.model = model;
        this.normal_shock = normal_shock;
        this.normal_pv = normal_pv;
        this.model.distributionManager.register(normal_shock);
        this.model.distributionManager.register(normal_pv);
    }

    public double getFundamental() {
        TimeInstant currentTime = model.getExperiment().getSimClock().getTime();
        if (lastTime != currentTime) {
            //Calculate a new fundamental
            double shock = normal_shock.sample();
            fundamental = Math.max(0, k * fundamental_mean + (1 - k) * fundamental + shock);
            n++;
            fundamental_mean = fundamental_mean + (fundamental - fundamental_mean) / n;
            lastTime = currentTime;
        }
        return fundamental;
    }

    public int getLimitPrice() {
        TimeInstant currentTime = model.getExperiment().getSimClock().getTime();
        if (lastTime != currentTime) {
            price_valuation = (int) Math.round(Math.max(0.0, getFundamental() + normal_pv.sample()));
        }
        return price_valuation;
    }
}
