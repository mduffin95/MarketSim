package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.SimClock;
import desmoj.core.statistic.ValueSupplier;

import java.util.Optional;
import java.util.Set;

/*
 * Route orders to the best exchange. Needs to use the graph to find connected entities.
 */
public class BestPriceOrderRouter implements OrderRouter {
    //public for testing purposes
    public MultiMarketView multiMarketView;
    private Exchange primary;
    private Set<Exchange> allExchanges;

    public ValueSupplier routingSupplier;

    //TODO: Clock may not be necessary
    public BestPriceOrderRouter(SimClock clock, Exchange exchange, Set<Exchange> allExchanges) {
//        this.model = model;
//        this.clock = clock;
        primary = exchange;
        multiMarketView = new MultiMarketView();
        this.allExchanges = allExchanges;
        this.routingSupplier = new BasicValueSupplier("RoutingSupplier");
    }

    /*
     * Returns the order that it sends. Send to exchange with the closest opposing order.
     */
    //TODO: Write some tests for this method
    @Override
    public Order routeOrder(TradingAgent agent, MessageType type, Direction direction, int price, int limit) {
        //We should always trust the individual market info more than the NBBO.

        Exchange e = findBestExchange(direction, price);

        Exchange trueBestExchange = findTrueBestExchange(direction, price);

        Order newOrder = new Order(agent, e, direction, price, limit);
        if (!e.equals(trueBestExchange))
            newOrder.inefficient = true;

        routingSupplier.notifyStatistics(newOrder);
        e.send(agent, type, newOrder);
        return newOrder;
    }

    public Exchange findTrueBestExchange(Direction direction, int price) {
        Order best = null;
        for (Exchange ex: allExchanges) {
            if (direction == Direction.BUY) {
                Order order = ex.getOrderBook().getBestSellOrder();
                if (null == order) continue;
                if (null == best) {
                    if (order.getPrice() < price)
                        best = order;
                } else if (order.getPrice() < best.getPrice() && order.getPrice() < price) {
                    best = order;
                }
            } else {
                Order order = ex.getOrderBook().getBestBuyOrder();
                if (null == order) continue;
                if (null == best) {
                    if (order.getPrice() > price)
                        best = order;
                } else if (order.getPrice() > best.getPrice() && order.getPrice() > price) {
                    best = order;
                }
            }
        }

        if (null == best)
            return primary;
        return best.getExchange();
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
            bestOffer = multiMarketView.getBestOffer().get().getOrder();
        } else {
            bestOffer = Optional.empty();
        }

        if (multiMarketView.getBestBid(primary).isPresent()) {
            primaryBestBid = multiMarketView.getBestBid(primary).get().getOrder();
        } else {
            primaryBestBid = Optional.empty();
        }
        if (multiMarketView.getBestOffer(primary).isPresent()) {
            primaryBestOffer = multiMarketView.getBestOffer(primary).get().getOrder();
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
            if ((bestBid.isPresent() && !primaryBestBid.isPresent()) ||
                    (bestBid.isPresent() && primaryBestBid.isPresent() && bestBid.get().getPrice() > primaryBestBid.get().getPrice())) {
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
