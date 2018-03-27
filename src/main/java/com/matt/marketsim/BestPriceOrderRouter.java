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
        Optional<Order> bestBid;
        Optional<Order> bestOffer;
        Optional<Order> primaryBestBid;
        Optional<Order> primaryBestOffer;
        if (multiMarketView.getBestBid().isPresent()) {
            bestBid = multiMarketView.getBestBid().get().getOrder();
        } else {
            bestBid = Optional.empty();
        }
        if (multiMarketView.getBestOffer().isPresent()) {
            bestOffer =  multiMarketView.getBestOffer().get().getOrder();
        } else {
            bestOffer = Optional.empty();
        }

        if (multiMarketView.getBestBid(primary).isPresent()) {
            primaryBestBid = multiMarketView.getBestBid(primary).get().getOrder();
        } else {
            primaryBestBid = Optional.empty();
        }
        if (multiMarketView.getBestOffer(primary).isPresent()) {
            primaryBestOffer =  multiMarketView.getBestOffer(primary).get().getOrder();
        } else {
            primaryBestOffer = Optional.empty();
        }

        if (direction == Direction.BUY) {
            if ((bestOffer.isPresent() && !primaryBestOffer.isPresent()) ||
                    (bestOffer.isPresent() && primaryBestOffer.isPresent() && bestOffer.get().getPrice() < primaryBestOffer.get().getPrice())) {
                //other price (usually NBBO) is better than the primary market.
                if (bestOffer.get().getPrice() < price) {
                    //Trade will transact immediately so send to other market.
                    return bestOffer.get().getExchange();
                } else {
                    //Trade will not transact immediately so send to primary market.
                    return primary;
                }
            } else {
                return primary;
            }
        } else {
            if (bestBid.isPresent() && primaryBestBid.isPresent() && bestBid.get().getPrice() > primaryBestBid.get().getPrice()) {
                //other price (usually NBBO) is better than primary market.
                if (bestBid.get().getPrice() > price) {
                    //Trade will transact immediately so send to other market.
                    return bestBid.get().getExchange();
                } else {
                    //Trade will not transact immediately so send to primary market.
                    return primary;
                }
            } else {
                return primary;
            }
        }
    }

    @Override
    public void respond(MarketUpdate update) {
        multiMarketView.add(update);
    }
}
