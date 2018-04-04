package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.dist.BoolDist;
import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.ContDist;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order currentOrder;
    private Limit limit;
    private ContDist offsetRange;

    public ZIC(MarketSimModel model, Limit limit, OrderRouter router, BoolDist direction, ContDist offsetRange, boolean showInTrace) {
        this(model, limit, router, direction.sample() ? Direction.BUY : Direction.SELL, offsetRange, showInTrace);
    }

    public ZIC(MarketSimModel model, Limit limit, OrderRouter router, Direction direction, ContDist offsetRange, boolean showInTrace) {
        super(model, router, showInTrace);

        model.registerForInitialSchedule(this); //Register so that it is scheduled
        this.limit = limit;
        this.offsetRange = offsetRange;
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

    private void placeOrder() {
        currentOrder = router.routeOrder(this, MessageType.LIMIT_ORDER, direction, newPrice, limit.getLimitPrice());
    }

    private int getNewPrice() {
        int price;
        if (direction == Direction.BUY) {
            price = Math.max(0, limit.getLimitPrice() - (int)Math.round(offsetRange.sample()));

        } else {
            price = limit.getLimitPrice() + (int)Math.round(offsetRange.sample());
        }
        return price;
    }

    @Override
    public void onLimitOrder(Order order) {

    }

    @Override
    public void onMarketOrder(Order order) {

    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {
        super.onOwnCompleted(update);
        this.active = false;
    }

    @Override
    public void onCancelOrder(Order order) {

    }

    @Override
    public void onCancelSuccess(Order order) {
        assert currentOrder == order;
        placeOrder();
    }

    @Override
    public void onCancelFailure(Order order) {

    }
}
