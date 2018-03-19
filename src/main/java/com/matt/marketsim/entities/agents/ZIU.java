package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.dist.DiscreteDist;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.dist.Distribution;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private Direction direction;
    private int limit;
    private OrderRouter router;
    private DiscreteDist randomPrice;

    public ZIU(MarketSimModel model, int limit, OrderRouter router, Direction direction, DiscreteDist randomPrice, boolean showInTrace) {
        super(model, router, showInTrace);
        this.direction = direction;
        this.limit = limit;
        this.randomPrice = randomPrice;
    }

    @Override
    public void doSomething() {
        int price = randomPrice.sample().intValue();

        router.routeOrder(this, MessageType.LIMIT_ORDER, direction, price, limit);
    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {
        super.onOwnCompleted(update);
        active = false;
    }

    @Override
    public void onCancelSuccess(Order order) {

    }

    @Override
    public void onCancelFailure(Order order) {

    }
}
