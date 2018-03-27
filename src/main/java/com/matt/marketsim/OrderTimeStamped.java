package com.matt.marketsim;

import desmoj.core.simulator.TimeInstant;

import java.util.Objects;
import java.util.Optional;

public class OrderTimeStamped {
    private Order order;
    private TimeInstant time;

    public OrderTimeStamped(TimeInstant time, Order order) {
        this.time = Objects.requireNonNull(time);
        this.order = order;
    }

    public TimeInstant getValidTime() {
        return time;
    }

    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    public boolean moreRecentThan(OrderTimeStamped order) {
        return TimeInstant.isAfterOrEqual(getValidTime(), order.getValidTime());
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!OrderTimeStamped.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final OrderTimeStamped other = (OrderTimeStamped) obj;
        if (null == this.order) {
            return null == other.order && this.time.equals(other.time);
        }
        return (this.time.equals(other.time) && (this.order == other.order || this.order.equals(other.order)));
    }

    @Override
    public String toString() {
        if (null == order)
            return "none";
        return order.toString();
    }
}
