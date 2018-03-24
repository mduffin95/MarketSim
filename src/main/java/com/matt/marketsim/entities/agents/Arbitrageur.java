package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import desmoj.core.simulator.Model;

/*
 * Doesn't use and order router. Instead handles the submission of orders itself.
 */
public class Arbitrageur extends TradingAgent {
    private QuoteData bestBid;
    private QuoteData bestOffer;
    private double alpha;

    public Arbitrageur(Model model, double alpha, boolean showInTrace) {
        super(model, null, showInTrace);
        this.alpha = alpha;
    }

    @Override
    public void doSomething() { //Shouldn't be called because we don't register to be scheduled
        throw new UnsupportedOperationException();
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
        QuoteData bid = update.summary.getBuyQuote();
        QuoteData offer = update.summary.getSellQuote();

        if ((null == bestBid ^ null == bid) ||
                null != bid &&
                        (bid.getPrice() > bestBid.getPrice() ||
                                (bid.getExchange().equals(bestBid.getExchange()) && !bid.equals(bestBid)))) {
            bestBid = bid;
        }
        if ((null == bestOffer ^ null == offer) ||
                null != offer &&
                        (offer.getPrice() < bestOffer.getPrice() ||
                                (offer.getExchange().equals(bestOffer.getExchange()) && !offer.equals(bestOffer)))) {
            bestOffer = offer;
        }

        if(checkArbitrage()) {
            int midpoint = (int)Math.floor((bestBid.getPrice() + bestOffer.getPrice()) / 2.0);
            Order b = new Order(this, bestOffer.getExchange(), Direction.BUY, midpoint, midpoint);
            Order s = new Order(this, bestBid.getExchange(), Direction.SELL, midpoint, midpoint);
            bestBid.getExchange().send(this, MessageType.LIMIT_ORDER, s);
            bestOffer.getExchange().send(this, MessageType.LIMIT_ORDER, b);
            sendTraceNote("Arbitrage opportunity, bestBid = " + bestBid.getPrice() + ", bestOffer = " + bestOffer.getPrice() + ", midpoint = " + midpoint);
        }
    }

    @Override
    public void onCancelSuccess(Order order) {
        //Shouldn't need to cancel as trades will execute immediately
    }

    @Override
    public void onCancelFailure(Order order) {

    }
}
