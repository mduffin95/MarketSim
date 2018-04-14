package com.matt.marketsim.statistics;

import com.matt.marketsim.Order;
import com.matt.marketsim.Trade;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.*;
import desmoj.core.statistic.StatisticObject;

import java.util.ArrayList;
import java.util.Observable;

public class TradeStatistics extends StatisticObject {
    private double totalUtility = 0.0;
    private TimeSpan totalExecutionTime;
    private TradingAgentGroup group;
    private double discountRate;
    private SimClock clock;
    private int totalOrders;

    private int inefficient;
    private int equilibrium;

    ArrayList<Double> deviation = new ArrayList<>();

    public TradeStatistics(Model model, String name, TradingAgentGroup group, double discountRate, SimClock clock, boolean showInReport, boolean showInTrace, int equilibrium) {
        super(model, name, showInReport, showInTrace);
        this.group = group;
        this.discountRate = discountRate;
        this.clock = clock;

        this.totalExecutionTime = new TimeSpan(0);
        this.totalOrders = 0;
        this.inefficient = 0;
        this.equilibrium = equilibrium;
    }

    public TradeStatistics(Model model, String name, TradingAgentGroup group, double discountRate, SimClock clock, boolean showInReport, boolean showInTrace) {
        this(model, name, group, discountRate, clock, showInReport, showInTrace, 0);
    }

    @Override
    public void update(Observable observable, Object arg) {

        if (arg instanceof Trade) {

            Trade trade = (Trade) arg;

            if (group.contains(trade.getBuyer())) {
                findInefficient(trade);
                processOrder(trade.getBuyOrder(), trade.getExecutionTime(), trade.getPrice());
            }
            if (group.contains(trade.getSeller())) {
                findInefficient(trade);
                processOrder(trade.getSellOrder(), trade.getExecutionTime(), trade.getPrice());
            }

            deviation.add(Math.pow(trade.getPrice() - equilibrium, 2));
        }
    }

    private void findInefficient(Trade trade) {
        int buyLimit = trade.getBuyOrder().getLimit();
        int sellLimit = trade.getSellOrder().getLimit();

        if (buyLimit > equilibrium && sellLimit > equilibrium ||
                buyLimit < equilibrium && sellLimit < equilibrium) {
            inefficient++;
        }
    }

    private void processOrder(Order order, TimeInstant executionTime, int price) {
        totalOrders++;
        TimeSpan t = TimeOperations.diff(executionTime, order.getArrivalTime());

        //Add to total execution time
        totalExecutionTime = TimeOperations.add(t, totalExecutionTime);

        //Apply discounting and add to total utility
        double coeff = Math.exp(-1 * discountRate * t.getTimeAsDouble());
        int util = Math.abs(order.getLimit() - price);
        totalUtility += coeff * util;

    }

    public double getTotalUtility() {
        return totalUtility;
    }

    public TimeSpan getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getInefficient() {
        return inefficient;
    }

    public double getPriceDiscovery() {
        double total = 0;
        for (Double p: deviation) {
            total += p;
        }

        return Math.sqrt(total / deviation.size());
    }

    @Override
    public Reporter createDefaultReporter() {
        return new TradeStatisticReporter(this);
    }

}
