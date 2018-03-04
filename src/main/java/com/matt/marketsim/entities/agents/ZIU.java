package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.LimitProvider;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private Direction direction;

    public ZIU(Model model, LimitProvider limit, OrderRouter router, Direction direction) {
        super(model, limit, router);
        this.direction = direction;

    }

    @Override
    public void doSomething() {
        int price = marketSimModel.getRandomPrice();

        router.routeOrder(this, MessageType.LIMIT_ORDER, direction, price);
    }

    @Override
    protected void respond(MarketUpdate update) {
        return;
    }

    @Override
    protected void cancelSuccess(Order order) {
        throw new UnsupportedOperationException();
    }

}
