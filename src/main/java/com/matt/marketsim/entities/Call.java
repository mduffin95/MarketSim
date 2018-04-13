package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import com.matt.marketsim.events.CallClearingEvent;
import desmoj.core.report.Message;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.LinkedList;
import java.util.Queue;

public class Call extends Exchange {
    private TimeSpan clearingInterval;
    private LOBSummary summary;

    public Call(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace, TimeSpan clearingInterval) {
        super(model, name, sip, showInTrace);
        this.clearingInterval = clearingInterval;
        Event<Call> event = new CallClearingEvent(model, "CallClearingEvent", true);
        event.schedule(this, clearingInterval);
    }

    public void clear() {
        //Clearing logic

        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();

        if (null == b || null == s)
            return;

        Queue<Trade> newTrades = new LinkedList<>();

        int midPrice = (int)Math.round((b.getPrice() + s.getPrice()) / 2.0);
        while (null != b && null != s && b.getPrice() >= s.getPrice()) {
            Trade t = new Trade(presentTime(), midPrice, 1, b, s);
            newTrades.add(t);
            lastTradeSupplier.notifyStatistics(t);
            sendTraceNote("Trade at " + t.getPrice());

            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
            b = orderBook.getBestBuyOrder();
            s = orderBook.getBestSellOrder();
        }

        LOBSummary newSummary = orderBook.getSummary(clock);
        if (!newSummary.equals(summary) || newTrades.size() != 0) {
            for (Trade t: newTrades) {
                MarketUpdate update = new MarketUpdate(this, t, newSummary);
                recentTrade = t; //For statistics. Only last one in queue will persist.

                //The price quote has changed so this needs to be sent to all observers
                MessageType msg = MessageType.MARKET_UPDATE;

                for (NetworkEntity e: observers) {
                    e.send(this, msg, update);
                }
            }
        }
        summary = newSummary;
    }

    @Override
    public void onLimitOrder(Order order) {
        order.setArrivalTime(clock.getTime());
        orderBook.add(order);

        String note = "Handling order: " + order.toString();
        sendTraceNote(note);
    }

    @Override
    public void onCancelOrder(Order order) {

    }

    public TimeSpan getClearingInterval() {
        return clearingInterval;
    }
}
