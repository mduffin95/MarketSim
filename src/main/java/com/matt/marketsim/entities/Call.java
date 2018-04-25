package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import com.matt.marketsim.events.CallClearingEvent;
import desmoj.core.report.Message;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class Call extends Exchange {
    private TimeSpan clearingInterval;
    private LOBSummary summary;
    private int option = 2;

    public Call(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace, TimeSpan clearingInterval) {
        super(model, name, sip, showInTrace);
        this.clearingInterval = clearingInterval;
        if (clearingInterval.getTimeAsDouble() > 0.0) {
            Event<Call> event = new CallClearingEvent(model, "CallClearingEvent", true);
            event.schedule(this, clearingInterval);
        }
        summary = orderBook.getSummary(clock);
    }

    public void clear() {
        //Clearing logic

        if (!orderBook.canTrade())
            return;

//        Queue<Trade> newTrades = new LinkedList<>();

        int clearPrice = orderBook.findIntersectionPrice().get();
        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();
        while (null != b && null != s && b.getPrice() >= s.getPrice()) {
            Trade t = new Trade(presentTime(), clearPrice, 1, b, s);
//            newTrades.add(t);
            lastTradeSupplier.notifyStatistics(t);
            sendTraceNote("Trade at " + t.getPrice());

            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
            summary = orderBook.getSummary(clock);
            sendUpdate(t, summary);
            recentTrade = t; //Only used for testing

            b = orderBook.getBestBuyOrder();
            s = orderBook.getBestSellOrder();
        }

//        LOBSummary newSummary = orderBook.getSummary(clock);
//        if (!newSummary.equals(summary) || newTrades.size() != 0) {
//            for (Trade t: newTrades) {
//                recentTrade = t; //For statistics. Only last one in queue will persist.
//                sendUpdate(t, newSummary);
//            }
//        }
//        summary = newSummary;
        updateSpreadStats(summary);
    }

    @Override
    public void onLimitOrder(Order order) {
        order.setArrivalTime(clock.getTime());
        orderBook.add(order);

        if (clearingInterval.getTimeAsDouble() == 0.0) {
            LOBSummary newSummary = orderBook.getSummary(clock);
            if (!summary.equals(newSummary))
                clear();
        }

//        else if (option == 2) {
//            LOBSummary newSummary = orderBook.getSummary(clock);
//            if (!newSummary.equals(summary)) {
//                summary = newSummary;
//                sendUpdate(null, newSummary);
//            }
//        }
        String note = "Handling order: " + order.toString();
        sendTraceNote(note);
    }

    public TimeSpan getClearingInterval() {
        return clearingInterval;
    }
}
