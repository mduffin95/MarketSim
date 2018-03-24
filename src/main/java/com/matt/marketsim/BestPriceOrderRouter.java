package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.SimClock;

import java.util.HashSet;
import java.util.Set;

/*
 * Route orders to the best exchange. Needs to use the graph to find connected entities.
 */
public class BestPriceOrderRouter implements OrderRouter {
    //public for testing purposes
    public MultiMarketView multiMarketView;
    private Exchange primary; //Used when we don't yet have a best bid or offer

    private Set<MarketUpdate> previousUpdates;
//    private SimClock clock;

    //TODO: Clock may not be necessary
    public BestPriceOrderRouter(SimClock clock, Exchange exchange) {
//        this.model = model;
//        this.clock = clock;
        primary = exchange;
        multiMarketView = new MultiMarketView();
        previousUpdates = new HashSet<>();
    }

    /*
     * Returns the order that it sends. Send to exchange with the closest opposing order.
     */
    //TODO: Write some tests for this method
    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price, int limit) {
        //TODO: This still has an issue when an out of date bid from our primary exchange is sent to us from the SIP.
        //We should always trust the individual market info more than the NBBO.

        Exchange e = findBestExchange(type, direction, price);

        Order newOrder = new Order(agent, e, direction, price, limit);
        e.send(agent, type, newOrder);
        return newOrder;
    }

    //Public for testing purposes
    public Exchange findBestExchange(MessageType type, Direction direction, int price) {
        Order bestOffer = multiMarketView.getBestOffer().order;
        Order bestBid = multiMarketView.getBestBid().order;
        Order primaryBestOffer = multiMarketView.getBestOffer(primary).order;
        Order primaryBestBid = multiMarketView.getBestBid(primary).order;
        Exchange e;
        if (direction == Direction.BUY) {
            //TODO: Need a better way of comparing these orders which can be null
            if ((null != bestOffer && null == primaryBestOffer) || lessThan(bestOffer, primaryBestOffer)) {
                //NBBO price is better than the primary market.
                if (bestOffer.getPrice() < price) {
                    //Trade will transact immediately so send to other market.
                    e = bestOffer.getExchange();
                } else {
                    //Trade will not transact immediately so send to primary market.
                    e = primary;
                }
            } else {
                e = primary;
            }
        } else {
            if (greaterThan(bestBid, primaryBestBid)) {
                //NBBO price is better than primary market.
                if (bestBid.getPrice() > price) {
                    //Trade will transact immediately so send to other market.
                    e = bestBid.getExchange();
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

    private boolean greaterThan(Order a, Order b) {
        if (null != a && null == b) {
            return true;
        }
        if (null == a) {
            return false;
        }
        return a.getPrice() > b.getPrice();
    }

    private boolean lessThan(Order a, Order b) {
        if (null == a && null != b) {
            return true;
        }
        if (null == b) {
            return false;
        }
        return a.getPrice() < b.getPrice();
    }

    @Override
    public void respond(MarketUpdate update) {
        if (previousUpdates.add(update)) {
            //Did not already contain this update (so we haven't seen it yet)
            multiMarketView.add(update);
        }

    }
}
