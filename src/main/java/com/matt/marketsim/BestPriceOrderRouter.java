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
    private MultiMarketView multiMarketView;
    private Exchange primary; //Used when we don't yet have a best bid or offer
    private SimClock clock;

    public BestPriceOrderRouter(SimClock clock, Exchange exchange) {
//        this.model = model;
        this.clock = clock;
        primary = exchange;
        multiMarketView = new MultiMarketView();
    }

    /*
     * Returns the order that it sends. Send to exchange with the closest opposing order.
     */
    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price, int limit) {
        //TODO: This still has an issue when an out of date bid from our primary exchange is sent to us from the SIP.
        //We should always trust the individual market info more than the NBBO.
        IOrder bestOffer = multiMarketView.getBestOffer();
        IOrder bestBid = multiMarketView.getBestBid();
        IOrder primaryBestOffer = multiMarketView.getBestOffer(primary);
        IOrder primaryBestBid = multiMarketView.getBestBid(primary);
        Exchange e;
        if (direction == Direction.BUY) {
            if (lessThan(bestOffer, primaryBestOffer)) {
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

        Order newOrder = new Order(agent, e, direction, price, limit, clock.getTime());
        e.send(agent, type, newOrder);
        return newOrder;
    }

    private boolean greaterThan(IOrder a, IOrder b) {
        if (null != a && null == b) {
            return true;
        }
        if (null == a) {
            return false;
        }
        return a.getPrice() > b.getPrice();
    }

    private boolean lessThan(IOrder a, IOrder b) {
        if (null == a && null != b) {
            return true;
        }
        if (null == b) {
            return false;
        }
        return a.getPrice() < b.getPrice();
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
        multiMarketView.add(update);
    }
}
