package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.LimitProvider;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import desmoj.core.simulator.Model;

/*
 * Doesn't use and order router. Instead handles the submission of orders itself.
 */
public class Arbitrageur extends TradingAgent {
    private Order bestBid;
    private Order bestOffer;

    public Arbitrageur(Model model) {
        super(model, null, null);
    }

    @Override
    public void doSomething() {
        return;
    }

    @Override
    protected void respond(MarketUpdate update) {
        Order buy = update.summary.getBestBuyOrder();
        Order sell = update.summary.getBestSellOrder();

        if (null == bestBid || buy.getPrice() > bestBid.getPrice()) {
            bestBid = buy;
        }
        if (null == bestOffer || sell.getPrice() < bestOffer.getPrice()) {
            bestOffer = sell;
        }

        if(checkArbitrage()) {
            int midpoint = (int)Math.floor((bestBid.getPrice() + bestOffer.getPrice()) / 2.0);
            Order b = new Order(this, bestOffer.getExchange(), Direction.BUY, midpoint);
            Order s = new Order(this, bestBid.getExchange(), Direction.SELL, midpoint);
            bestBid.getExchange().send(this, MessageType.LIMIT_ORDER, s);
            bestOffer.getExchange().send(this, MessageType.LIMIT_ORDER, b);
        }
    }

    //TODO: This needs the same logic as the SIP
    private boolean checkArbitrage() {
        if (null == bestBid || null == bestOffer) return false;

        if (bestBid.getPrice() > bestOffer.getPrice()) {
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

    //Shouldn't need to cancel as trades will execute immediately.
    @Override
    protected void cancelSuccess(Order order) {
        return;
    }
}
