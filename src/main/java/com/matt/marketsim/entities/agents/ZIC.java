package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.LimitProvider;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order previousOrder;

    public ZIC(Model model, LimitProvider limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
        super(model, limit, e, sip);
        this.direction = direction;
    }

    private Order newOrder;
    @Override
    public void doSomething() {
        newOrder = getOrder();
        if (active) {
            if (null == previousOrder) {
                placeOrder();
            } else {
                if (direction == Direction.BUY && newOrder.getPrice() > previousOrder.getPrice() ||
                        direction == Direction.SELL && newOrder.getPrice() < previousOrder.getPrice()) {
                    primaryExchange.send(this, MessageType.CANCEL, previousOrder);

                }
            }

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
        assert previousOrder == order;
        placeOrder();
    }

    private void placeOrder() {
        primaryExchange.send(this, MessageType.LIMIT_ORDER, newOrder);
        previousOrder = newOrder;
    }

    private Order getOrder() {
        int price;
        if (direction == Direction.BUY) {
            price = marketSimModel.generator.nextInt(limit.getLimitPrice() + 1);

        } else {
            price = limit.getLimitPrice() + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit.getLimitPrice() + 1);
        }
        return new Order(this, primaryExchange, direction, price);
    }
}
