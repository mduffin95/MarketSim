package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import com.sun.org.apache.xpath.internal.operations.Quo;
import desmoj.core.simulator.SimClock;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/*
 * Route orders to the best exchange. Needs to use the graph to find connected entities.
 */
public class BestPriceOrderRouter implements OrderRouter {
    //public for testing purposes
    public MultiMarketView multiMarketView;
    private Exchange primary;

    //TODO: Clock may not be necessary
    public BestPriceOrderRouter(SimClock clock, Exchange exchange) {
//        this.model = model;
//        this.clock = clock;
        primary = exchange;
        multiMarketView = new MultiMarketView();
    }

    /*
     * Returns the order that it sends. Send to exchange with the closest opposing order.
     */
    //TODO: Write some tests for this method
    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price, int limit) {
        //TODO: This still has an issue when an out of date bid from our primary exchange is sent to us from the SIP.
        //We should always trust the individual market info more than the NBBO.

        Exchange e = findBestExchange(direction, price);

        Order newOrder = new Order(agent, e, direction, price, limit);
        e.send(agent, type, newOrder);
        return newOrder;
    }

    //Public for testing purposes
    public Exchange findBestExchange(Direction direction, int price) {
        Optional<QuoteData> bestOfferOpt = multiMarketView.getBestOffer();
        Optional<QuoteData> bestBidOpt = multiMarketView.getBestBid();
        Optional<QuoteData> primaryBestOfferOpt = multiMarketView.getBestOffer(primary);
        Optional<QuoteData> primaryBestBidOpt = multiMarketView.getBestBid(primary);
        Exchange e;
        if (direction == Direction.BUY) {
            //TODO: Need a better way of comparing these orders which can be null
            if ((bestOfferOpt.isPresent() && !primaryBestOfferOpt.isPresent()) || QuoteData.lessThan(bestOfferOpt.get(), primaryBestOfferOpt.get())) {
                //NBBO price is better than the primary market.
                if (bestOfferOpt.get().getPrice() < price) {
                    //Trade will transact immediately so send to other market.
                    e = bestOfferOpt.get().getExchange();
                } else {
                    //Trade will not transact immediately so send to primary market.
                    e = primary;
                }
            } else {
                e = primary;
            }
        } else {
            if (QuoteData.greaterThan(bestBidOpt.get(), primaryBestBidOpt.get())) {
                //NBBO price is better than primary market.
                if (bestBidOpt.get().getPrice() > price) {
                    //Trade will transact immediately so send to other market.
                    e = bestBidOpt.get().getExchange();
                } else {
                    //Trade will not transact immediately so send to primary market.
                    e = primary;
                }
            } else {
                //Send to primary market
                e = primary;
            }
        }
        return e;
    }

    @Override
    public void respond(MarketUpdate update) {
        multiMarketView.add(update);
    }
}
