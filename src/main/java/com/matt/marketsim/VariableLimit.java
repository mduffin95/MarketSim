package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.TimeInstant;

public class VariableLimit implements LimitProvider {
    private static double k;
    private static double fundamental_mean;
    private static int n;
    private static double fundamental;
    private static double sigma_shock;
    private static double sigma_pv;
    private static ContDistNormal normal_shock;
    private static ContDistNormal normal_pv;
    private static MarketSimModel model;
    private static TimeInstant lastTime = null;

    //Member variables
    private int price_valuation;


    public static void init(MarketSimModel model, double sigma_shock, double sigma_pv, double k, double initial_fundamental) {
        assert (k >= 0 && k <= 1);
        VariableLimit.n = 1;
        VariableLimit.fundamental_mean = initial_fundamental;
        VariableLimit.fundamental = initial_fundamental;
        VariableLimit.k = k;
        VariableLimit.model = model;
        VariableLimit.sigma_shock = sigma_shock;
        VariableLimit.sigma_pv = sigma_pv;
        VariableLimit.normal_shock = new ContDistNormal(VariableLimit.model, "Shock", 0, VariableLimit.sigma_shock, true, false);
        VariableLimit.normal_pv = new ContDistNormal(VariableLimit.model, "Price valuation", 0, VariableLimit.sigma_pv, true, false);
        VariableLimit.model.distributionManager.register(normal_shock);
        VariableLimit.model.distributionManager.register(normal_pv);
    }

    public static double getFundamental() {
        TimeInstant currentTime = model.getExperiment().getSimClock().getTime();
        if (lastTime != currentTime) {
            //Calculate a new fundamental
             double shock = normal_shock.sample();
             fundamental = Math.max(0, k * fundamental_mean + (1-k) * fundamental + shock);
             n++;
             fundamental_mean = fundamental_mean + (fundamental - fundamental_mean) / n;
             lastTime = currentTime;
        }
        return fundamental;
    }


    @Override
    public int getLimitPrice(Order order) {
        TimeInstant currentTime = model.getExperiment().getSimClock().getTime();
        if (lastTime != currentTime) {
            price_valuation = (int)Math.round(Math.max(0.0, getFundamental() + normal_pv.sample()));
        }
        return price_valuation;
    }
}
