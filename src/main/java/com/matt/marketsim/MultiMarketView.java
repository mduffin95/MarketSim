package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;

import java.util.HashMap;
import java.util.Map;

public class MultiMarketView {
    private Map<NetworkEntity, OrderTimeStamped> bidSummaryMap;
    private Map<NetworkEntity, OrderTimeStamped> offerSummaryMap;
    private Map<NetworkEntity, LOBSummary> summaryMap;

    public MultiMarketView() {
        summaryMap = new HashMap<>();
    }

    //TODO: should not be source of update, but exchange within order.
    public void add(MarketUpdate update) {
        summaryMap.put(update.source, update.summary);
    }

    public OrderTimeStamped getBestBid() {
        OrderTimeStamped bestBid = null;
        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaryMap.entrySet()) {
            OrderTimeStamped bid = entry.getValue().getBestBuyOrder();
            if (null == bestBid || bid != null && bid.order.getPrice() > bestBid.order.getPrice() &&
                    (bid.order.getExchange() != bestBid.order.getExchange() || TimeInstant.isAfterOrEqual(bid.time, bestBid.time))) {
                bestBid = bid;
            }
        }
        return bestBid;
    }

    public OrderTimeStamped getBestOffer() {
        OrderTimeStamped bestOffer = null;
        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaryMap.entrySet()) {
            OrderTimeStamped offer = entry.getValue().getBestSellOrder();
            if (null == bestOffer || offer != null && offer.order.getPrice() < bestOffer.order.getPrice() &&
                    (offer.order.getExchange() != bestOffer.order.getExchange() || TimeInstant.isAfterOrEqual(offer.time, bestOffer.time))) {
                bestOffer = offer;
            }
        }
        return bestOffer;
    }

    public OrderTimeStamped getBestBid(NetworkEntity e) {
        OrderTimeStamped result = null;
        LOBSummary tmp = summaryMap.get(e);
        if (tmp != null) {
            result = tmp.getBestBuyOrder();
        }
        return result;
    }

    public OrderTimeStamped getBestOffer(NetworkEntity e) {
        OrderTimeStamped result = null;
        LOBSummary tmp = summaryMap.get(e);
        if (tmp != null) {
            result = tmp.getBestSellOrder();
        }
        return result;
    }
}