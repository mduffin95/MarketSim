package com.matt.marketsim;

import java.util.HashMap;
import java.util.Map;

public class StoredLimit implements LimitProvider {

    Map<Order, Integer> limits;

    public StoredLimit() {
        limits = new HashMap<>();
    }

    public void setLimitPrice(Order order, int limit) {
        limits.put(order, limit);
    }

    @Override
    public int getLimitPrice(Order order) {
        return limits.get(order);
    }
}
