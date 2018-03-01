package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order bestOrder;

    public ZIC(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
        super(model, limit, e, sip);
        this.direction = direction;
    }

    @Override
    public void doSomething() {
        Order newOrder = getOrder();

        if (bestOrder == null ||
                direction == Direction.BUY && newOrder.getPrice() > bestOrder.getPrice() ||
                direction == Direction.SELL && newOrder.getPrice() < bestOrder.getPrice()) {
            primaryExchange.send(this, MessageType.CANCEL, bestOrder);
            primaryExchange.send(this, MessageType.LIMIT_ORDER, newOrder);
            bestOrder = newOrder;
        }
    }

    @Override
    protected void respond(MarketUpdate update) {
        if (isMyTrade(update.trade)) {
            //Was a buyer or a seller in this trade
            this.active = false;
        }
    }

    @Override
    protected void cancelSuccess(Order order) {
        throw new UnsupportedOperationException();
    }

    private Order getOrder() {
        int price;
        if (direction == Direction.BUY) {
            price = marketSimModel.generator.nextInt(limit + 1);

        } else {
            price = limit + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit + 1);
        }
        return new Order(this, primaryExchange, direction, price);
    }
}
