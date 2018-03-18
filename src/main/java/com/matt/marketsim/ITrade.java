package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;

public interface ITrade {
    int getPrice();
    int getQuantity();
    Order getBuyOrder();
    Order getSellOrder();
    TradingAgent getBuyer();
    TradingAgent getSeller();
}
