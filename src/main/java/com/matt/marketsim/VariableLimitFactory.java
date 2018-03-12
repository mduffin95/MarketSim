package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.dist.ContDistNormal;

public class VariableLimitFactory {
    private MarketSimModel model;
    private double sigma_shock;
    private double sigma_pv;
    private double k;
    private double initial_fundamental;
    private ContDistNormal normal_shock;
    private ContDistNormal normal_pv;

    public VariableLimitFactory(MarketSimModel model, double sigma_shock, double sigma_pv, double k, double initial_fundamental) {
        this.model = model;
        this.k = k;
        this.model = model;
        this.sigma_shock = sigma_shock;
        this.sigma_pv = sigma_pv;
        this.initial_fundamental = initial_fundamental;
        this.normal_shock = new ContDistNormal(this.model, "Shock", 0, this.sigma_shock, true, false);
        this.normal_pv = new ContDistNormal(this.model, "Price valuation", 0, this.sigma_pv, true, false);
    }

    public VariableLimit create() {
        return new VariableLimit(model, sigma_shock, sigma_pv, k, initial_fundamental, normal_shock, normal_pv);
    }
}
