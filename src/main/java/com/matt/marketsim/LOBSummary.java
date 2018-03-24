package com.matt.marketsim;

import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;

public class LOBSummary {
    public OrderTimeStamped buyOrder;
    public OrderTimeStamped sellOrder;

    public LOBSummary() {
    }

    public LOBSummary(TimeInstant time, Order buyOrder, Order sellOrder) {
        this.buyOrder = new OrderTimeStamped(time, buyOrder);
        this.sellOrder = new OrderTimeStamped(time, sellOrder);
    }

    public LOBSummary(OrderTimeStamped buyOrder, OrderTimeStamped sellOrder) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
    }

    public OrderTimeStamped getBestBuyOrder() {
        return buyOrder;
    }

    public OrderTimeStamped getBestSellOrder() {
        return sellOrder;
    }
}
