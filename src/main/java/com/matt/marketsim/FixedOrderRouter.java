package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.SimClock;

public class FixedOrderRouter implements OrderRouter {
    private SimClock clock;
    private Exchange primary;
    public FixedOrderRouter(SimClock clock, Exchange exchange) {
        this.clock = clock;
        primary = exchange;
    }

    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price, int limit) {
        Order order = new Order(agent, primary, direction, price, limit);
        primary.send(agent, MessageType.LIMIT_ORDER, order);
        return order;
    }

    @Override
    public void respond(MarketUpdate update) {
        return;
    }
}
