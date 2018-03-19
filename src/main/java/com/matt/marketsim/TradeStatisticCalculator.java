package com.matt.marketsim;

import com.matt.marketsim.dtos.TradeStatisticDto;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.*;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

public class TradeStatisticCalculator extends StatisticObject {
    private double totalUtility = 0.0;
    private TimeSpan executionTime;
    private TradingAgentGroup group;
    private double discountRate;
    private SimClock clock;

    private int totalOrders;

    public TradeStatisticCalculator(Model model, String name, TradingAgentGroup group, double discountRate, SimClock clock, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
        this.group = group;
        this.discountRate = discountRate;
        this.clock = clock;

        this.executionTime = new TimeSpan(0);
        this.totalOrders = 0;
    }

    @Override
    public void update(Observable observable, Object arg) {

        if (arg instanceof Trade) {
            TimeInstant currentTime = clock.getTime();
            Trade trade = (Trade) arg;
            if (group.contains(trade.getBuyer())) {
                totalOrders++;
                TimeSpan t = TimeOperations.diff(currentTime, trade.getBuyOrder().getArrivalTime());

                //Add to total execution time
                executionTime = TimeOperations.add(t, executionTime);

                //Apply discounting and add to total utility
                double coeff = Math.exp(-1 * discountRate * t.getTimeAsDouble());
                totalUtility += coeff * (trade.getBuyOrder().getLimit() - trade.getPrice());
            }
            if (group.contains(trade.getSeller())) {
                totalOrders++;
                TimeSpan t = TimeOperations.diff(currentTime, trade.sellOrder.getArrivalTime());

                //Add to total execution time
                executionTime = TimeOperations.add(t, executionTime);

                //Apply discounting and add to total utility
                double coeff = Math.exp(-1 * discountRate * t.getTimeAsDouble());
                totalUtility += coeff * (trade.getPrice() - trade.sellOrder.getLimit());
            }
        }
    }

    public double getTotalUtility() {
        return totalUtility;
    }

    public TimeSpan getTotalExecutionTime() {
        return executionTime;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public TradeStatisticDto getResults() {
        TradeStatisticDto result = new TradeStatisticDto();
        result.name = getName();
        result.totalUtility = getTotalUtility();
        result.totalExecutionTime = getTotalExecutionTime().getTimeAsDouble();
        result.totalOrders = getTotalOrders();
        return result;
    }

    @Override
    public Reporter createDefaultReporter() {
        return new TradeStatisticReporter(this);
    }

}
