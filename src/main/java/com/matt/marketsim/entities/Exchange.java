package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;
import desmoj.core.statistic.ValueSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public abstract class Exchange extends NetworkEntity implements PriceProvider {

    OrderBook orderBook;
    ValueSupplier lastTradeSupplier;
    //Entities that need to be notified of price changes
    List<NetworkEntity> observers;



    //For testing purposes
    public Trade recentTrade;

    SimClock clock;

    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        orderBook = new OrderBook();

        registerPriceObserver(sip);
        lastTradeSupplier = new BasicValueSupplier("LastTradeSupplier");
        clock = model.getExperiment().getSimClock();
    }

    /**
     * Register a network entity to this exchange. This means the network entity will be sent price updates.
     */
    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        if (null != networkEntity)
            observers.add(networkEntity);
    }

    public void registerLastTradeObserver(Observer o) {
        lastTradeSupplier.addObserver(o);
    }

    public void printQueues() {
        orderBook.printOrderBook();
    }

    public OrderBook getOrderBook() {
        return orderBook;
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
    public void onCancelSuccess(Order order) {

    }

    @Override
    public void onCancelFailure(Order order) {

    }
}
