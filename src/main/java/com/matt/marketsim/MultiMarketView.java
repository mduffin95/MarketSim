package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;

import java.util.HashMap;
import java.util.Map;

public class MultiMarketView {
    private Map<NetworkEntity, LOBSummary> summaryMap;

    public MultiMarketView() {
        summaryMap = new HashMap<>();
    }

    public void add(MarketUpdate update) {
        summaryMap.put(update.source, update.summary);
    }

    public IOrder getBestBid() {
        IOrder bestBid = null;
        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaryMap.entrySet()) {
            IOrder bid = entry.getValue().getBestBuyOrder();
            if (null == bestBid || bid != null && bid.getPrice() > bestBid.getPrice()) {
                bestBid = bid;
            }
        }
        return bestBid;
    }

    public IOrder getBestOffer() {
        IOrder bestOffer = null;
        for (Map.Entry<NetworkEntity, LOBSummary> entry : summaryMap.entrySet()) {
            IOrder offer = entry.getValue().getBestSellOrder();
            if (null == bestOffer || offer != null && offer.getPrice() < bestOffer.getPrice()) {
                bestOffer = offer;
            }
        }
        return bestOffer;
    }

    public IOrder getBestBid(NetworkEntity e) {
        IOrder result = null;
        LOBSummary tmp = summaryMap.get(e);
        if (tmp != null) {
            result = tmp.getBestBuyOrder();
        }
        return result;
    }

    public IOrder getBestOffer(NetworkEntity e) {
        IOrder result = null;
        LOBSummary tmp = summaryMap.get(e);
        if (tmp != null) {
            result = tmp.getBestSellOrder();
        }
        return result;
    }
}
