package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import desmoj.core.simulator.Model;

import java.util.Objects;
import java.util.Optional;

/*
 * Doesn't use and order router. Instead handles the submission of orders itself.
 */
public class Arbitrageur extends TradingAgent {
    private MultiMarketView multiMarketView;
    private double alpha;

    public Arbitrageur(Model model, double alpha, boolean showInTrace) {
        super(model, null, showInTrace);
        this.alpha = alpha;
        this.multiMarketView = new MultiMarketView();
    }

    @Override
    public void doSomething() { //Shouldn't be called because we don't register to be scheduled
        throw new UnsupportedOperationException();
    }

    private boolean checkArbitrage(Order bestBid, Order bestOffer) {
        Objects.requireNonNull(bestBid);
        Objects.requireNonNull(bestOffer);

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
        multiMarketView.add(update);
        Optional<Order> bestBidOpt = Optional.empty();
        Optional<Order> bestOfferOpt = Optional.empty();
        Optional<OrderTimeStamped> bestBidTimeStamped = multiMarketView.getBestBid();
        Optional<OrderTimeStamped> bestOfferTimeStamped = multiMarketView.getBestOffer();

        if (bestBidTimeStamped.isPresent()) {
            bestBidOpt = bestBidTimeStamped.get().getOrder();
        }
        if (bestOfferTimeStamped.isPresent()) {
            bestOfferOpt = bestOfferTimeStamped.get().getOrder();
        }

        if(bestBidOpt.isPresent() && bestOfferOpt.isPresent()) {
            Order bestBid = bestBidOpt.get();
            Order bestOffer = bestOfferOpt.get();
            if (checkArbitrage(bestBid, bestOffer)) {
                int midpoint = (int) Math.floor((bestBid.getPrice() + bestOffer.getPrice()) / 2.0);
                Order b = new Order(this, bestOffer.getExchange(), Direction.BUY, midpoint, midpoint);
                Order s = new Order(this, bestBid.getExchange(), Direction.SELL, midpoint, midpoint);
                bestBid.getExchange().send(this, MessageType.LIMIT_ORDER, s);
                bestOffer.getExchange().send(this, MessageType.LIMIT_ORDER, b);
                sendTraceNote("Arbitrage opportunity, bestBid = " + bestBid.getPrice() + ", bestOffer = " + bestOffer.getPrice() + ", midpoint = " + midpoint);
            }
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
