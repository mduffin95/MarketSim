package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;

public class VariableLimit implements Limit {
    private ContDistNormal normal_pv;
    private MarketSimModel model;
    private TimeInstant lastTime = null;
    private VariableLimitFactory factory;
    private SimClock clock;
    private int price_valuation;

    public VariableLimit(VariableLimitFactory factory, SimClock clock, ContDistNormal normal_pv) {
        this.factory = factory;
        this.normal_pv = normal_pv;
        this.clock = clock;
    }

    public int getLimitPrice() {
        TimeInstant currentTime = clock.getTime();
        if (lastTime != currentTime) {
            price_valuation = (int) Math.round(Math.max(0.0, factory.getFundamental() + normal_pv.sample()));
            lastTime = currentTime;
        }
        return price_valuation;
    }
}
