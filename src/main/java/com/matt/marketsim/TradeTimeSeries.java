package com.matt.marketsim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.TimeSeries;

import java.util.Observable;

public class TradeTimeSeries extends TimeSeries {
    public TradeTimeSeries(Model model, String s, String s1, TimeInstant timeInstant, TimeInstant timeInstant1, boolean b, boolean b1) {
        super(model, s, s1, timeInstant, timeInstant1, b, b1);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Trade) {
            super.update(o, ((Trade) arg).price);
        }
    }
}
