package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.SimClock;

public interface OrderRouter {
    Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price);
//    void routeOrder(Order order);
    void respond(MarketUpdate update);
}
