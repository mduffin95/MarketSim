package com.matt.marketsim;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

public class RoutingStatistics extends StatisticObject {
    TradingAgentGroup group;
    int total = 0;
    int inefficent = 0;
    public RoutingStatistics(Model model, String s, TradingAgentGroup group, boolean showInReport, boolean showInTrace) {
        super(model, s, showInReport, showInTrace);
        this.group = group;
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (arg instanceof Order) {
            Order order = (Order)arg;
            if (group.contains(order.getAgent())) {
                total++; //We only notify when routing is inefficient
                if (order.inefficient)
                    inefficent++;
            }
        }
    }

    public int getInefficient() {
        return inefficent;
    }

    public int getTotalOrders() {
        return total;
    }

    public double getInefficientFraction() {
        return inefficent / (double) total;
    }

    @Override
    public Reporter createDefaultReporter() {
        return new RoutingStatisticReporter(this);
    }
}
