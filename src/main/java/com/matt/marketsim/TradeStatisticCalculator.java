package com.matt.marketsim;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

public class TradeStatisticCalculator extends StatisticObject {
    private int totalUtility = 0;
    private boolean equilibriumSet = false;
    private int equilibrium;
    private long sumOfSquares = 0;
    private int count = 0;
    private TradingAgentGroup group;

    public TradeStatisticCalculator(Model model, String name, TradingAgentGroup group, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
        this.group = group;
    }

    public TradeStatisticCalculator(Model model, String name, TradingAgentGroup group, int equilibrium, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
        this.equilibrium = equilibrium;
        equilibriumSet = true;
        this.group = group;
    }

    @Override
    public void update(Observable observable, Object arg) {

        if (arg instanceof Trade) {
            Trade trade = (Trade) arg;
            if (group.contains(trade.buyer)) {
                count++;
                totalUtility += trade.buyer.limit.getLimitPrice() - trade.price;
                sumOfSquares += Math.pow(trade.price - equilibrium, 2);
            }
            if (group.contains(trade.seller)) {
                count++;
                totalUtility += trade.price - trade.seller.limit.getLimitPrice();
                sumOfSquares += Math.pow(trade.price - equilibrium, 2);
            }
        }
    }

    public double getSmithsAlpha() {
        if (!equilibriumSet) {
            return 0;
        }
        if (count == 0 || equilibrium == 0) return 0;
        return Math.sqrt(sumOfSquares / count) / equilibrium;
    }

    public double getAllocEfficiency() {
        try {
            return totalUtility / (double) group.getTheoreticalUtility();
        } catch (UnsupportedOperationException e) {
            return 0;
        }

    }

    public double getTotalUtility() { return totalUtility; }

    @Override
    public Reporter createDefaultReporter() {
        return new TradeStatisticReporter(this);
    }


//    public void setEquilibrium(int equilibrium) {
//        this.equilibrium = equilibrium;
//        equilibriumSet = true;
//    }
}
