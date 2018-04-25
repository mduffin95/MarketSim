package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import desmoj.core.report.Message;
import desmoj.core.simulator.*;
import com.matt.marketsim.MessageType;
import desmoj.core.statistic.ValueSupplier;

import java.util.*;

public class CDA extends Exchange {

    public CDA(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, sip, showInTrace);

    }

    @Override
    public void onLimitOrder(Order order) {
        order.setArrivalTime(clock.getTime());

        String note = "Handling order: " + order.toString();
        sendTraceNote(note);

        LOBSummary original = orderBook.getSummary(clock);
        orderBook.add(order);

        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();

        Trade newTrade = null;

        if (null != b && null != s && b.getPrice() >= s.getPrice()) {
            int price;
            if (order.getDirection() == Direction.BUY) {
                price = s.getPrice();
            } else {
                price = b.getPrice();
            }
            newTrade = new Trade(presentTime(), price, 1, b, s);

            //Update utility and record on time series
//            marketSimModel.recordTrade(newTrade);
            lastTradeSupplier.notifyStatistics(newTrade);
            sendTraceNote("Trade at " + newTrade.getPrice());

            //Remove from the order book
            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
        }

        LOBSummary newSummary = orderBook.getSummary(clock);
        updateSpreadStats(newSummary);
        if (!newSummary.equals(original) || newTrade != null) { //Not sure you will end up in a situation where newtrade != null but the summaries are equal.
            sendUpdate(newTrade, newSummary);
        }
        recentTrade = newTrade;
    }

}