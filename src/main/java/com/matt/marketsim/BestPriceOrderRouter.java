package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.SimClock;

/*
 * Route orders to the best exchange. Needs to use the graph to find connected entities.
 */
public class BestPriceOrderRouter implements OrderRouter {
//    private MarketSimModel model;
    private Order bestBid;
    private Order bestOffer;
    private Exchange primary; //Used when we don't yet have a best bid or offer
    private SimClock clock;

    public BestPriceOrderRouter(SimClock clock, Exchange exchange) {
//        this.model = model;
        this.clock = clock;
        primary = exchange;
    }

    /*
     * Returns the order that it sends. Send to exchange with the closest opposing order.
     */
    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price) {
        Exchange e;
        if (direction == Direction.BUY && bestOffer != null) {
            e = bestOffer.getExchange();
        } else if (direction == Direction.SELL && bestBid != null) {
            e = bestBid.getExchange();
        } else {
            e = primary;
        }
        Order newOrder = new Order(agent, e, direction, price, clock.getTime());
        e.send(agent, type, newOrder);
        return newOrder;
    }

//    @Override
//    public void routeOrder(Order order) {
//        if (order.direction == Direction.BUY) {
//            order.setExchange()
//        } else {
//            newOrder = new Order(agent, bestBid.getExchange(), direction, price);
//        }
//        newOrder.getExchange().send(agent, type, newOrder);
//        return newOrder;
//    }

    @Override
    public void respond(MarketUpdate update) {
        Order bid = update.summary.getBestBuyOrder();
        Order offer = update.summary.getBestSellOrder();

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
    }
}
