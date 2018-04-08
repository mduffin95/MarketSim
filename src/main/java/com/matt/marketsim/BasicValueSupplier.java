package com.matt.marketsim;

import desmoj.core.statistic.ValueSupplier;

public class BasicValueSupplier extends ValueSupplier {

    public BasicValueSupplier(String s) {
        super(s);
    }

    @Override
    public double value() {
        return 0;
    }
}
