package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import desmoj.core.simulator.Model;

/*
 * Doesn't use and order router. Instead handles the submission of orders itself.
 */
public class Arbitrageur extends TradingAgent {
    private IOrder bestBid;
    private IOrder bestOffer;
    private double alpha;

    public Arbitrageur(Model model, double alpha, boolean showInTrace) {
        super(model, null, showInTrace);
        this.alpha = alpha;
    }

    @Override
    public void doSomething() {
        return;
    }

    private boolean checkArbitrage() {
        if (null == bestBid || null == bestOffer) return false;

        if (bestBid.getPrice() > (1.0 + alpha) * bestOffer.getPrice()) {
            if (bestBid.getExchange() != bestOffer.getExchange()) {
                //Arbitrage opportunity exists.
                return true;
            } else {
                //Shouldn't happen
                throw new RuntimeException("Prices should have matched on the same exchange.");
            }
        }
        return false;
    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {

    }

    @Override
    public void onMarketUpdate(MarketUpdate update) {
        IOrder bid = update.summary.getBestBuyOrder();
        IOrder offer = update.summary.getBestSellOrder();

        if ((null == bestBid ^ null == bid) ||
                null != bid &&
                        (bid.getPrice() > bestBid.getPrice() ||
                                (bid.getExchange() == bestBid.getExchange() && bid != bestBid))) {
            bestBid = bid;
        }
        if ((null == bestOffer ^ null == offer) ||
                null != offer &&
                        (offer.getPrice() < bestOffer.getPrice() ||
                                (offer.getExchange() == bestOffer.getExchange() && offer != bestOffer))) {
            bestOffer = offer;
        }

        if(checkArbitrage()) {
            int midpoint = (int)Math.floor((bestBid.getPrice() + bestOffer.getPrice()) / 2.0);
            IOrder b = new Order(this, bestOffer.getExchange(), Direction.BUY, midpoint, midpoint, clock.getTime());
            IOrder s = new Order(this, bestBid.getExchange(), Direction.SELL, midpoint, midpoint, clock.getTime());
            bestBid.getExchange().send(this, MessageType.LIMIT_ORDER, s);
            bestOffer.getExchange().send(this, MessageType.LIMIT_ORDER, b);
        }
    }

    @Override
    public void onCancelSuccess(IOrder order) {
        //Shouldn't need to cancel as trades will execute immediately
    }

    @Override
    public void onCancelFailure(IOrder order) {

    }
}
