package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import sun.nio.ch.Net;

import java.util.*;

public class MultiMarketView {
    private Map<NetworkEntity, LOBSummary> summaries;

    public MultiMarketView() {
        summaries = new HashMap<>();
    }


    public void add(MarketUpdate update) {
        Objects.requireNonNull(update);
        LOBSummary summary = update.getSummary();

        OrderTimeStamped buy = summary.getBuyOrder();
        OrderTimeStamped sell = summary.getSellOrder();

        LOBSummary oldSummary = summaries.get(update.getSource());
        if (null == oldSummary) {
            summaries.put(update.getSource(), summary);
            return;
        }
        updateSummary(buy, sell, oldSummary);
    }

    /**
     *
      * @param buy - not null - the buy order
     * @param sell - not null - the sell order
     * @param summary - not null - the summary that needs to be checked and updated
     */
    private void updateSummary(OrderTimeStamped buy, OrderTimeStamped sell, LOBSummary summary) {
        if (buy.moreRecentThan(summary.getBuyOrder())) {
            summary.setBuyOrder(buy);
        }

        if (sell.moreRecentThan(summary.getSellOrder())) {
            summary.setSellOrder(sell);
        }
    }

    /**
     *
     * @return
     */
    public Optional<OrderTimeStamped> getBestBid() {
        Map<Exchange, OrderTimeStamped> orderMap = new HashMap<>();

        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaries.entrySet()) {
            LOBSummary summary = entry.getValue();
            OrderTimeStamped newOts = summary.getBuyOrder();
            Optional<Order> bid = newOts.getOrder();
            if (bid.isPresent()) {
                Exchange e = bid.get().getExchange();
                OrderTimeStamped currentOts = orderMap.get(e);
                if (null == currentOts || newOts.moreRecentThan(currentOts)) {
                    orderMap.put(e, newOts);
                }
            }
        }
        //We now have the most recent order from each exchange in a map
        //Every OrderTimeStamped in the map has an Order, as otherwise it would not have been entered

        OrderTimeStamped bestBid = null;
        for (Map.Entry<Exchange, OrderTimeStamped> entry : orderMap.entrySet()) {
            OrderTimeStamped ots = entry.getValue();
            Order bid = ots.getOrder().get();
            if (null == bestBid || (bid.getPrice() > bestBid.getOrder().get().getPrice())) {
                bestBid = ots;
            }
        }

        return Optional.ofNullable(bestBid);
    }

    public Optional<OrderTimeStamped> getBestOffer() {
        Map<Exchange, OrderTimeStamped> orderMap = new HashMap<>();

        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaries.entrySet()) {
            LOBSummary summary = entry.getValue();
            OrderTimeStamped newOts = summary.getSellOrder();
            Optional<Order> offer = newOts.getOrder();
            if (offer.isPresent()) {
                Exchange e = offer.get().getExchange();
                OrderTimeStamped currentOts = orderMap.get(e);
                if (null == currentOts || newOts.moreRecentThan(currentOts)) {
                    orderMap.put(e, newOts);
                }
            }
        }
        //We now have the most recent order from each exchange in a map
        //Every OrderTimeStamped in the map has an Order, as otherwise it would not have been entered

        OrderTimeStamped bestOffer = null;
        for (Map.Entry<Exchange, OrderTimeStamped> entry : orderMap.entrySet()) {
            OrderTimeStamped ots = entry.getValue();
            Order bid = ots.getOrder().get();
            if (null == bestOffer || (bid.getPrice() > bestOffer.getOrder().get().getPrice())) {
                bestOffer = ots;
            }
        }

        return Optional.ofNullable(bestOffer);
    }

    public Optional<OrderTimeStamped> getBestBid(NetworkEntity e) {
        //Shouldn't be a key in both maps
        LOBSummary summary = summaries.get(e);

        if (null != summary)
            return Optional.of(summary.getBuyOrder());

        return Optional.empty();
    }

    public Optional<OrderTimeStamped> getBestOffer(NetworkEntity e) {
        //Shouldn't be a key in both maps
        LOBSummary summary = summaries.get(e);

        if (null != summary)
            return Optional.of(summary.getSellOrder());

        return Optional.empty();
    }
}