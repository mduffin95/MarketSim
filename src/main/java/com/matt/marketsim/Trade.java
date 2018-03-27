package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;

public class Trade {
    public TimeInstant time;
    private int price;
    private int quantity;
    private Order buyOrder;
    public Order sellOrder;

    public Trade() {

    }

    public Trade(TimeInstant time, int price, int quantity, Order buyOrder, Order sellOrder) {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Order getBuyOrder() {
        return buyOrder;
    }

    public Order getSellOrder() {
        return sellOrder;
    }

    public TradingAgent getBuyer() {
        return buyOrder.getAgent();
    }

    public TradingAgent getSeller() {
        return sellOrder.getAgent();
    }
}