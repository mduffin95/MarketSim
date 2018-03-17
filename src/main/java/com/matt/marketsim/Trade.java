package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;

public class Trade implements ITrade {
    public TimeInstant time;
//    public com.matt.marketsim.entities.Exchange exchange;
    private int price;
    private int quantity;
    private IOrder buyOrder;
    public IOrder sellOrder;


    public Trade(TimeInstant time, int price, int quantity, IOrder buyOrder, IOrder sellOrder) {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public IOrder getBuyOrder() {
        return buyOrder;
    }

    @Override
    public IOrder getSellOrder() {
        return sellOrder;
    }

    @Override
    public TradingAgent getBuyer() {
        return buyOrder.getAgent();
    }

    @Override
    public TradingAgent getSeller() {
        return sellOrder.getAgent();
    }
}