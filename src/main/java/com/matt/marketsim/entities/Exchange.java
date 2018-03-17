package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;
import com.matt.marketsim.MessageType;
import desmoj.core.statistic.ValueSupplier;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {
    private MarketSimModel marketSimModel;

    private OrderBook orderBook;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    public LastTradeSupplier lastTradeSupplier;

    //For testing purposes
    public Trade recentTrade;

    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        observers = new ArrayList<>();
        orderBook = new OrderBook();

        registerPriceObserver(sip);
        lastTradeSupplier = new LastTradeSupplier("LastTradeSupplier");
    }

    @Override
    public void onLimitOrder(IOrder order) {
        String note = "Handling order: " + order.toString();
        sendTraceNote(note);

        LOBSummary original = orderBook.getSummary(1);
        orderBook.add(order);

        IOrder b = orderBook.getBestBuyOrder();
        IOrder s = orderBook.getBestSellOrder();

        Trade newTrade = null;

        if (null != b && null != s && b.getPrice() >= s.getPrice()) {
            int price;
            if (order.getDirection() == Direction.BUY) {
                price = s.getPrice();
            } else {
                price = b.getPrice();
            }
            TimeInstant currentTime = marketSimModel.getExperiment().getSimClock().getTime();
            newTrade = new Trade(currentTime, price, 1, b, s);

            //Update utility and record on time series
//            marketSimModel.recordTrade(newTrade);
            lastTradeSupplier.notifyStatistics(newTrade);
            sendTraceNote("Trade at " + newTrade.getPrice());

            //Remove from the order book
            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
        }

        LOBSummary newSummary = orderBook.getSummary(1);
        if (newSummary.getBestBuyOrder() != original.getBestBuyOrder() ||
                newSummary.getBestSellOrder() != original.getBestSellOrder() ||
                newTrade != null) {
            MarketUpdate update = new MarketUpdate(newTrade, newSummary);
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
    public void onMarketOrder(IOrder order) {

    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {

    }

    @Override
    public void onMarketUpdate(MarketUpdate update) {

    }

    @Override
    public void onCancelOrder(IOrder order) {
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
    public void onCancelSuccess(IOrder order) {

    }

    @Override
    public void onCancelFailure(IOrder order) {

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
} /* end of model class */