package com.matt.marketsim;

import desmoj.core.simulator.TimeInstant;

import java.util.Objects;
import java.util.Optional;

public class LOBSummary {

    //These are always present, although the orders within them may not be.
    private OrderTimeStamped buyOrder;
    private OrderTimeStamped sellOrder;

    public LOBSummary() {
    }

    public LOBSummary(TimeInstant time, Order buyOrder, Order sellOrder) {
        this.buyOrder = new OrderTimeStamped(time, buyOrder);
        this.sellOrder = new OrderTimeStamped(time, sellOrder);
    }

    public LOBSummary(OrderTimeStamped buyOrder, OrderTimeStamped sellOrder) {
        this.buyOrder = Objects.requireNonNull(buyOrder);
        this.sellOrder = Objects.requireNonNull(sellOrder);
    }

    public OrderTimeStamped getBuyOrder() {
        return buyOrder;
    }

    public OrderTimeStamped getSellOrder() {
        return sellOrder;
    }

    public void setBuyOrder(OrderTimeStamped ots) {
        this.buyOrder = ots;
    }

    public void setSellOrder(OrderTimeStamped ots) {
        this.sellOrder = ots;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!LOBSummary.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final LOBSummary other = (LOBSummary) obj;
        if ((this.buyOrder == null) ? (other.buyOrder != null) : !this.buyOrder.equals(other.buyOrder)) {
            return false;
        }
        if ((this.sellOrder == null) ? (other.sellOrder != null) : !this.sellOrder.equals(other.sellOrder)) {
            return false;
        }
        return true;
    }
}
