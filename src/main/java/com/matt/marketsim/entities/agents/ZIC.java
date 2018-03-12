package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order currentOrder;
    private VariableLimit limit;

    public ZIC(Model model, VariableLimit limit, OrderRouter router, Direction direction) {
        super(model, router);
        this.direction = direction;
        this.limit = limit;
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

    private void placeOrder() {
        currentOrder = router.routeOrder(this, MessageType.LIMIT_ORDER, direction, newPrice, limit.getLimitPrice());
    }

    private int getNewPrice() {
        int price;
        if (direction == Direction.BUY) {
            price = Math.max(0, limit.getLimitPrice() - (int)Math.round(marketSimModel.offsetRange.sample()));

        } else {
            price = limit.getLimitPrice() + (int)Math.round(marketSimModel.offsetRange.sample());
        }
        return price;
    }

    @Override
    protected void onLimitOrder(Order order) {

    }

    @Override
    protected void onMarketOrder(Order order) {

    }

    @Override
    protected void onOwnCompleted(MarketUpdate update) {
        super.onOwnCompleted(update);
        this.active = false;
    }

    @Override
    protected void onCancelOrder(Order order) {

    }

    @Override
    protected void onCancelSuccess(Order order) {
        assert currentOrder == order;
        placeOrder();
    }

    @Override
    protected void onCancelFailure(Order order) {

    }
}
