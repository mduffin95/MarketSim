package com.matt.marketsim.statistics;

import com.matt.marketsim.Trade;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.TimeSeries;

import java.util.Observable;

public class TradeTimeSeries extends TimeSeries {
    private TradingAgentGroup group;
    public TradeTimeSeries(Model model, String s, TradingAgentGroup group, String s1, TimeInstant timeInstant, TimeInstant timeInstant1, boolean b, boolean b1) {
        super(model, s, s1, timeInstant, timeInstant1, b, b1);
        this.group = group;
    }

    public TradeTimeSeries(Model model, String s, TradingAgentGroup group, TimeInstant timeInstant, TimeInstant timeInstant1, boolean b, boolean b1) {
        super(model, s, timeInstant, timeInstant1, b, b1);
        this.group = group;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Trade) {
            Trade trade = (Trade) arg;
            if (group.contains(trade.getBuyer()) || group.contains(trade.getSeller())) {
                super.update(o, ((Trade) arg).getPrice());
            }
        }
    }
}
