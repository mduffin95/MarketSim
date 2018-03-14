package com.matt.marketsim;

import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.*;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

public class TradeStatisticCalculator extends StatisticObject {
    private double totalUtility = 0.0;
    private boolean equilibriumSet = false;
//    private int equilibrium;
    private long sumOfSquares = 0;
    private int count = 0;
    private TradingAgentGroup group;
    private double discountRate;
    private SimClock clock;
    private TimeUnit timeUnit;

    public TradeStatisticCalculator(Model model, String name, TradingAgentGroup group, double discountRate, SimClock clock, TimeUnit timeUnit, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
        this.group = group;
        this.discountRate = discountRate;
        this.clock = clock;
        this.timeUnit = timeUnit;
    }

//    public TradeStatisticCalculator(Model model, String name, TradingAgentGroup group, double discountRate, int equilibrium, boolean showInReport, boolean showInTrace) {
//        super(model, name, showInReport, showInTrace);
//        this.equilibrium = equilibrium;
//        equilibriumSet = true;
//        this.group = group;
//        this.discountRate = discountRate;
//    }

    @Override
    public void update(Observable observable, Object arg) {

        if (arg instanceof Trade) {
            TimeInstant currentTime = clock.getTime();
            Trade trade = (Trade) arg;
            if (group.contains(trade.buyer)) {
                count++;
                TimeSpan t = TimeOperations.diff(currentTime, trade.buyOrder.getTimeStamp());
                double coeff = Math.exp(-1 * discountRate * t.getTimeAsDouble(timeUnit));
                totalUtility += coeff * (trade.buyOrder.getLimit() - trade.price);
//                if (equilibriumSet)
//                    sumOfSquares += Math.pow(trade.price - equilibrium, 2);
            }
            if (group.contains(trade.seller)) {
                count++;
                TimeSpan t = TimeOperations.diff(currentTime, trade.sellOrder.getTimeStamp());
                double coeff = Math.exp(-1 * discountRate * t.getTimeAsDouble(timeUnit));
                totalUtility += coeff * (trade.price - trade.sellOrder.getLimit());
//                if (equilibriumSet)
//                    sumOfSquares += Math.pow(trade.price - equilibrium, 2);
            }
        }
    }

//    public double getSmithsAlpha() {
//        if (!equilibriumSet) {
//            return 0;
//        }
//        if (count == 0 || equilibrium == 0) return 0;
//        return Math.sqrt(sumOfSquares / count) / equilibrium;
//    }

//    public double getAllocEfficiency() {
//        try {
//            return totalUtility / (double) group.getTheoreticalUtility();
//        } catch (UnsupportedOperationException e) {
//            return 0;
//        }
//
//    }

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
