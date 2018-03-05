package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;

public class Trade {
    public TimeInstant time;
//    public com.matt.marketsim.entities.Exchange exchange;
    public int price;
    public int quantity;
    public Order buyOrder;
    public Order sellOrder;
    public TradingAgent buyer;
    public TradingAgent seller;


    public Trade(TimeInstant time, int price, int quantity, Order buyOrder, Order sellOrder) {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.buyer = buyOrder.agent;
        this.seller = sellOrder.agent;
    }
}