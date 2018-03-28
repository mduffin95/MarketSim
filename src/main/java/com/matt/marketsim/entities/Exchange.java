package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import desmoj.core.simulator.*;
import com.matt.marketsim.MessageType;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {

    private OrderBook orderBook;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    public LastTradeSupplier lastTradeSupplier;

    //For testing purposes
    public Trade recentTrade;

    private SimClock clock;

    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        orderBook = new OrderBook();

        registerPriceObserver(sip);
        lastTradeSupplier = new LastTradeSupplier("LastTradeSupplier");
        clock = model.getExperiment().getSimClock();
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
        if (!newSummary.equals(original) || newTrade != null) { //Not sure you will end up in a situation where newtrade != null but the summaries are equal.
            MarketUpdate update = new MarketUpdate(this, newTrade, newSummary);
            MessageType msg;

            //The price quote has changed so this needs to be sent to all observers
            msg = MessageType.MARKET_UPDATE;

            for (NetworkEntity e: observers) {
                e.send(this, msg, update);
            }
        }
        recentTrade = newTrade;
    }

    @Override
    public void onMarketOrder(Order order) {

    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {

    }

    @Override
    public void onMarketUpdate(MarketUpdate update) {

    }

    @Override
    public void onCancelOrder(Order order) {
        if (null != order) {
            sendTraceNote("Cancelling order: " + order.toString());
            boolean success = orderBook.remove(order);
            if (success) {
                order.getAgent().send(this, MessageType.CANCEL_SUCCESS, order);
            } else {
                order.getAgent().send(this, MessageType.CANCEL_FAILURE, order);
            }
        }
    }

    @Override
    public void onCancelSuccess(Order order) {

    }

    @Override
    public void onCancelFailure(Order order) {

    }

    /**
     * Register a network entity to this exchange. This means the network entity will be sent price updates.
     */
    public void registerPriceObserver(NetworkEntity networkEntity) {
        if (null != networkEntity)
            observers.add(networkEntity);
    }


    public void printQueues() {
        orderBook.printOrderBook();
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

}