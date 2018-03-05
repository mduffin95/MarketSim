package com.matt.marketsim;

public interface LimitProvider {

    //Need to provide the order as there may be different limit prices for different orders (e.g. Arbitrageur)
    int getLimitPrice(Order order);
}
