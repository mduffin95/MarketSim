package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private Direction direction;
    private int limit;
    private OrderRouter router;

    public ZIU(Model model, int limit, OrderRouter router, Direction direction) {
        super(model, router);
        this.direction = direction;
        this.limit = limit;
    }

    @Override
    public void doSomething() {
        int price = marketSimModel.getRandomPrice();

        router.routeOrder(this, MessageType.LIMIT_ORDER, direction, price, limit);
    }

    @Override
    protected void onOwnCompleted(MarketUpdate update) {
        super.onOwnCompleted(update);
        active = false;
    }

    @Override
    protected void onCancelSuccess(Order order) {

    }

    @Override
    protected void onCancelFailure(Order order) {

    }
}
