package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;

/*
 * Route orders to the best exchange. Needs to use the graph to find connected entities.
 */
public class BestPriceOrderRouter implements OrderRouter {
//    private MarketSimModel model;
    private Order bestBid;
    private Order bestOffer;
    private Exchange primary; //Used when we don't yet have a best bid or offer

    public BestPriceOrderRouter(Exchange exchange) {
//        this.model = model;
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
        Order newOrder = new Order(agent, e, direction, price);
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
        Order buy = update.summary.getBestBuyOrder();
        Order sell = update.summary.getBestSellOrder();

        if (null == bestBid || buy.getPrice() > bestBid.getPrice()) {
            bestBid = buy;
        }
        if (null == bestOffer || sell.getPrice() < bestOffer.getPrice()) {
            bestOffer = sell;
        }
    }
}
