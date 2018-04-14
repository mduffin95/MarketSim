package com.matt.marketsim.statistics;

import com.matt.marketsim.LOBSummary;
import com.matt.marketsim.Order;
import com.matt.marketsim.OrderTimeStamped;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Optional;

public class ExchangeStatistics extends StatisticObject {
    ArrayList<Double> midQuotes = new ArrayList<>();
    ArrayList<Integer> spreads = new ArrayList<>();


    public ExchangeStatistics(Model model, String name, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    public void volatilityUpdate(LOBSummary summary) {
        Optional<Order>  buy = summary.getBuyOrder().getOrder();
        Optional<Order>  sell = summary.getSellOrder().getOrder();

        if (!buy.isPresent() || !sell.isPresent())
            return;
        double mid = (buy.get().getPrice() + sell.get().getPrice()) / 2.0;
        midQuotes.add(mid);
    }

    public void spreadUpdate(LOBSummary summary) {
        Optional<Order>  buy = summary.getBuyOrder().getOrder();
        Optional<Order>  sell = summary.getSellOrder().getOrder();

        if (!buy.isPresent() || !sell.isPresent())
            return;

        int spread = sell.get().getPrice() - buy.get().getPrice();
        assert spread >= 0;
        spreads.add(spread);
    }

    public double getMedianSpread() {
        Collections.sort(spreads);
        int len = spreads.size();
        int middle = len / 2;

        if (len % 2 == 1) {
            return spreads.get(middle);
        } else {
            return (spreads.get(middle-1) + spreads.get(middle) / 2.0);
        }
    }

    public double getVolatility() {
        return Math.log(sd(midQuotes));
    }

    @Override
    public Reporter createDefaultReporter() {
        return new ExchangeStatisticReporter(this);
    }

    private static double sd(ArrayList<Double> table)
    {
        // Step 1:
        double mean = mean(table);
        double temp = 0;

        for (int i = 0; i < table.size(); i++)
        {
            double val = table.get(i);

            // Step 2:
            double squrDiffToMean = Math.pow(val - mean, 2);

            // Step 3:
            temp += squrDiffToMean;
        }

        // Step 4:
        double meanOfDiffs = temp / table.size();

        // Step 5:
        return Math.sqrt(meanOfDiffs);
    }

    private static double mean(ArrayList<Double> table)
    {
        double total = 0;

        for ( int i= 0;i < table.size(); i++)
        {
            double currentNum = table.get(i);
            total+= currentNum;
        }
        return total / table.size();
    }

}
