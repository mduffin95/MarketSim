package com.matt.marketsim;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

public class TradeStatisticCalculator extends StatisticObject {
    private int totalUtility = 0;
    private int theoreticalUtility;
    private int equilibrium;
    private long sumOfSquares = 0;
    private int count = 0;

    public TradeStatisticCalculator(Model model, String name, int tag, int equilibrium, int theoreticalUtility, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
        this.theoreticalUtility = theoreticalUtility;
        this.equilibrium = equilibrium;
    }

    @Override
    public void update(Observable observable, Object arg) {

        if (arg instanceof Trade) {
            Trade trade = (Trade) arg;
            count++;

            totalUtility += trade.buyer.limit - trade.seller.limit;
            sumOfSquares += Math.pow(trade.price - equilibrium, 2);
        }
    }

    public double getSmithsAlpha() {
        if (count == 0 || equilibrium == 0) return 0;
        return Math.sqrt(sumOfSquares / count) / equilibrium;
    }

    public double getAllocEfficiency() {
        return totalUtility / (double) theoreticalUtility;
    }

    @Override
    public Reporter createDefaultReporter() {
        return new TradeStatisticReporter(this);
    }
}
