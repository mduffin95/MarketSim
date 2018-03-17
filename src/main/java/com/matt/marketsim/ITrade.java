package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;

public interface ITrade {
    int getPrice();
    int getQuantity();
    IOrder getBuyOrder();
    IOrder getSellOrder();
    TradingAgent getBuyer();
    TradingAgent getSeller();
}
