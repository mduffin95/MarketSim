package com.matt.marketsim.builders;

public class FixedLimit implements LimitProvider {
    private int limit;
    public FixedLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public int getLimitPrice() {
        return limit;
    }
}
