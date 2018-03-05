package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;

public class FixedOrderRouter implements OrderRouter {
    Exchange primary;
    public FixedOrderRouter(Exchange exchange) {
        primary = exchange;
    }

    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price) {
        Order order = new Order(agent, primary, direction, price);
        primary.send(agent, MessageType.LIMIT_ORDER, order);
        return order;
    }

    @Override
    public void respond(MarketUpdate update) {
        return;
    }
}
