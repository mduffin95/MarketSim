package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;

import java.util.Random;

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
    private Random generator;


    public ZIP(MarketSimModel model, int limit, OrderRouter router, Direction direction, Random generator, boolean showInTrace) {
        super(model, router, showInTrace);
        ((MarketSimModel) model).registerForInitialSchedule(this); //Register so that it is scheduled
        this.limit = limit;
        this.direction = direction;
        this.generator = generator;
//        learning_rate = 0.25;
        momentum = 0.1 * generator.nextDouble();
        beta = 0.1 + 0.4 * generator.nextDouble();

        if (direction == Direction.BUY) {
            margin = -1.0 * (0.05 + 0.3 * generator.nextDouble());
        } else {
            margin = 0.05 + 0.3 * generator.nextDouble();
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
    public void onOwnCompleted(MarketUpdate update) {
        active = false;
        onMarketUpdate(update);
    }

    @Override
    public void onCancelOrder(Order order) {

    }

    @Override
    public void onCancelSuccess(Order order) {
        assert previousOrder == order;
        placeOrder();
    }

    @Override
    public void onCancelFailure(Order order) {

    }

    private void placeOrder() {
        previousOrder = router.routeOrder(this, MessageType.LIMIT_ORDER, direction, getPrice(), limit);
    }

    @Override
    public void onMarketUpdate(MarketUpdate update) {
        super.onMarketUpdate(update);
        //TODO: Make sure it only responds to price changes once (not duplicates from SIP).
        Trade trade = update.trade;
        LOBSummary summary = update.summary;

        boolean deal = trade != null;
        Direction lastOrderDirection = null;
        int price;

        QuoteData currentBestBuy = (null == currentSummary) ? null : currentSummary.getBuyQuote();
        QuoteData currentBestSell = (null == currentSummary) ? null : currentSummary.getSellQuote();

        if (currentBestBuy != summary.getBuyQuote()) {
            //Either new buy order or trade occurred that cleared with the buy order
            if (deal) {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = trade.getPrice();
            } else {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = summary.getBuyQuote().getPrice();
            }
        } else if (currentBestSell != summary.getSellQuote()) {
            //Either new sell order or trade occurred that cleared with the sell order
            if (deal) {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = trade.getPrice();
            } else {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = summary.getSellQuote().getPrice();
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
                if (getPrice() <= trade.getPrice()) {
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
                if (getPrice() >= trade.getPrice()) {
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
        double ptrb_abs = ca * generator.nextDouble();
        double ptrb_rel = (1 + cr * generator.nextDouble()) * price;

        return (int)Math.round(ptrb_rel + ptrb_abs);

    }

    private int target_down(int price) {
        double ptrb_abs = ca * generator.nextDouble();
        double ptrb_rel = (1 - cr * generator.nextDouble()) * price;

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
