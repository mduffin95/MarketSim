package com.matt.marketsim;

import desmoj.core.statistic.ValueSupplier;

public class LastTradeSupplier extends ValueSupplier {
    Trade lastTrade;

    public LastTradeSupplier(String s) {
        super(s);
    }

    @Override
    public void notifyStatistics(Object arg) {
        super.notifyStatistics(arg);

        lastTrade = (Trade) arg;
    }

    @Override
    public double value() {
        if (lastTrade != null)
            return (double) lastTrade.getPrice();
        return 0;
    }
}
