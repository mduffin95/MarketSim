package com.matt.marketsim;

public class FixedLimit implements Limit {

    private int limit;

    public FixedLimit(int limit) {
        this.limit = limit;
    }
    @Override
    public int getLimitPrice() {
        return limit;
    }
}
