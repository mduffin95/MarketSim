package com.matt.marketsim;

import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;

//An Order/Time pair used for quotes/updates
public class OrderTimeStamped {
    public Order order;
    public TimeInstant time;

    public OrderTimeStamped(TimeInstant time, Order order) {
        this.order = order;
        this.time = time;
    }
}
