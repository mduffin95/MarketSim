package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import desmoj.core.simulator.Model;

public class ZIP extends TradingAgent {
    private double margin;
    private double ca = 0.05;
    private double cr = 0.05;
    private double momentum;
    private double beta;
    private double prev_change;

//    private double learning_rate;
    private LOBSummary currentSummary;

    private Order previousOrder = null;
    private int limit;


    public ZIP(Model model, int limit, OrderRouter router, Direction direction) {
        super(model, router);
        this.limit = limit;
        this.direction = direction;
//        learning_rate = 0.25;
        momentum = 0.1 * marketSimModel.generator.nextDouble();
        beta = 0.1 + 0.4 * marketSimModel.generator.nextDouble();

        if (direction == Direction.BUY) {
            margin = -1.0 * (0.05 + 0.3 * marketSimModel.generator.nextDouble());
        } else {
            margin = 0.05 + 0.3 * marketSimModel.generator.nextDouble();
        }
    }

    @Override
    public void doSomething() {
        if (active) {
            if (null == previousOrder)
                placeOrder();
            else
                previousOrder.getExchange().send(this, MessageType.CANCEL, previousOrder);
        }
    }

    @Override
    protected void onOwnCompleted(MarketUpdate update) {
        active = false;
        onMarketUpdate(update);
    }

    @Override
    protected void onCancelOrder(Order order) {

    }

    @Override
    public void onCancelSuccess(Order order) {
        assert previousOrder == order;
        placeOrder();
    }

    @Override
    protected void onCancelFailure(Order order) {

    }

    private void placeOrder() {
        previousOrder = router.routeOrder(this, MessageType.LIMIT_ORDER, direction, getPrice(), limit);
    }

    @Override
    protected void onMarketUpdate(MarketUpdate update) {
        super.onMarketUpdate(update);
        //TODO: Make sure it only responds to price changes once (not duplicates from SIP).
        Trade trade = update.trade;
        LOBSummary summary = update.summary;

        boolean deal = trade != null;
        Direction lastOrderDirection = null;
        int price;

        Order currentBestBuy = (null == currentSummary) ? null : currentSummary.getBestBuyOrder();
        Order currentBestSell = (null == currentSummary) ? null : currentSummary.getBestSellOrder();

        if (currentBestBuy != summary.getBestBuyOrder()) {
            //Either new buy order or trade occurred that cleared with the buy order
            if (deal) {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = trade.price;
            } else {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = summary.getBestBuyOrder().getPrice();
            }
        } else if (currentBestSell != summary.getBestSellOrder()) {
            //Either new sell order or trade occurred that cleared with the sell order
            if (deal) {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = trade.price;
            } else {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = summary.getBestSellOrder().getPrice();
            }
        } else {
            //Nothing has changed
            return;
        }

        currentSummary = summary;

        int target;
        if (direction == Direction.SELL) {
            if (deal)  {
                //com.matt.marketsim.Trade has occurred.
                if (getPrice() <= trade.price) {
                    //increase profit margin
                    target = target_up(price);
                    updateMargin(target);
                } else if (active) {
                    //Being undercut - reduce profit margin
                    target = target_down(price);
                    updateMargin(target);
                }
            } else if (active && lastOrderDirection == Direction.SELL && getPrice() > price) {
                //Being undercut - reduce profit margin
                target = target_down(price);
                updateMargin(target);
            }

        } else {
            if (deal) {
                //com.matt.marketsim.Trade has occurred.
                if (getPrice() >= trade.price) {
                    //increase profit margin (lower bid price)
                    target = target_down(price);
                    updateMargin(target);
                } else if (active) {
                    //Price too low - reduce profit margin
                    target = target_up(price);
                    updateMargin(target);
                }
            } else if (active && lastOrderDirection == Direction.BUY && getPrice() < price) {
                //Price too low - reduce profit margin
                target = target_up(price);
                updateMargin(target);
            }
        }
//        sendTraceNote("Limit = " + limit + ", Price = " + getPrice());
    }

    private int target_up(int price) {
        double ptrb_abs = ca * marketSimModel.generator.nextDouble();
        double ptrb_rel = (1 + cr * marketSimModel.generator.nextDouble()) * price;

        return (int)Math.round(ptrb_rel + ptrb_abs);

    }

    private int target_down(int price) {
        double ptrb_abs = ca * marketSimModel.generator.nextDouble();
        double ptrb_rel = (1 - cr * marketSimModel.generator.nextDouble()) * price;

        return (int)Math.round(ptrb_rel - ptrb_abs);

    }

    private void updateMargin(int target) {
        int price = getPrice();
        double delta = beta * (target - price);
        double change = (momentum * prev_change) + (1.0 - momentum) * delta;
        prev_change = change;
        double newMargin = ((price + change) / getLimitPrice()) - 1;

        if (direction == Direction.BUY) {
            if (newMargin < 0.0) {
                margin = newMargin;
            }
        } else {
            if (newMargin > 0.0) {
                margin = newMargin;
            }
        }
    }

    private int getPrice() {
        int p = (int)Math.round(getLimitPrice() * (1 + margin));
        assert (direction == Direction.BUY && p <= getLimitPrice()) || (direction == Direction.SELL && p >= getLimitPrice());
        return p;
    }

    private int getLimitPrice() {
        return limit;
    }
}
