package com.matt.marketsim;

public class FixedLimit implements LimitProvider {
    private int limit;
    public FixedLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public int getLimitPrice(Order order) {
        return limit;
    }
}
