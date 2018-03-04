package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;

public interface OrderRouter {
    Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price);
//    void routeOrder(Order order);
    void respond(MarketUpdate update);
}
