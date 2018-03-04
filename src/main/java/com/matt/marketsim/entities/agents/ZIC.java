package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.LimitProvider;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order currentOrder;

    public ZIC(Model model, LimitProvider limit, OrderRouter router, Direction direction) {
        super(model, limit, router);
        this.direction = direction;
    }

    private int newPrice;
    @Override
    public void doSomething() {
        newPrice = getNewPrice();
        if (active) {
            if (null == currentOrder) {
                placeOrder();
            } else {
                if (direction == Direction.BUY && newPrice > currentOrder.getPrice() ||
                        direction == Direction.SELL && newPrice < currentOrder.getPrice()) {
                    currentOrder.getExchange().send(this, MessageType.CANCEL, currentOrder);

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
        assert currentOrder == order;
        placeOrder();
    }

    private void placeOrder() {
        currentOrder = router.routeOrder(this, MessageType.LIMIT_ORDER, direction, newPrice);
    }

    private int getNewPrice() {
        int price;
        if (direction == Direction.BUY) {
            price = marketSimModel.generator.nextInt(limit.getLimitPrice() + 1);

        } else {
            price = limit.getLimitPrice() + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit.getLimitPrice() + 1);
        }
        return price;
    }
}
