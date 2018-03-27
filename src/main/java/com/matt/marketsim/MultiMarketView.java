package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MultiMarketView {
    private Map<Exchange, LOBSummary> exchangeSummaries;
    private Map<SecuritiesInformationProcessor, LOBSummary> sipSummaries;

    public MultiMarketView() {
        exchangeSummaries = new HashMap<>();
        sipSummaries = new HashMap<>();
    }


    public void add(MarketUpdate update) {
        Objects.requireNonNull(update);
        LOBSummary summary = update.getSummary();

        OrderTimeStamped buy = summary.getBuyOrder();
        OrderTimeStamped sell = summary.getSellOrder();
        if (update.getSource() instanceof Exchange) {
            LOBSummary oldSummary = exchangeSummaries.get(update.getSource());
            if (null == oldSummary) {
                exchangeSummaries.put((Exchange)update.getSource(), summary);
                return;
            }
            updateSummary(buy, sell, oldSummary);
        } else if (update.getSource() instanceof SecuritiesInformationProcessor) {
            LOBSummary oldSummary = sipSummaries.get(update.getSource());
            if (null == oldSummary) {
                sipSummaries.put((SecuritiesInformationProcessor) update.getSource(), summary);
                return;
            }
            updateSummary(buy, sell, oldSummary);
        }
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

    public Optional<OrderTimeStamped> getBestBid() {
        OrderTimeStamped bestBid = null;
        for (Map.Entry<Exchange, LOBSummary> entry : exchangeSummaries.entrySet()) {
            LOBSummary summary = entry.getValue();
            Optional<Order> bid = summary.getBuyOrder().getOrder();
//            if (null == summary) {
//                continue;
//            }
            if (null == bestBid || !bestBid.getOrder().isPresent() || (bid.isPresent() && bid.get().getPrice() > bestBid.getOrder().get().getPrice())) {
                bestBid = summary.getBuyOrder();
            }
        }
        return Optional.ofNullable(bestBid);
    }

    public Optional<OrderTimeStamped> getBestOffer() {
        OrderTimeStamped bestOffer = null;
        for (Map.Entry<SecuritiesInformationProcessor, LOBSummary> entry : sipSummaries.entrySet()) {
            LOBSummary summary = entry.getValue();
            Optional<Order> offer = summary.getSellOrder().getOrder();
//            if (null == summary) {
//                continue;
//            }
            if (null == bestOffer || !bestOffer.getOrder().isPresent() || (offer.isPresent() && offer.get().getPrice() > bestOffer.getOrder().get().getPrice())) {
                bestOffer = summary.getSellOrder();
            }
        }
        return Optional.ofNullable(bestOffer);
    }

    public Optional<OrderTimeStamped> getBestBid(NetworkEntity e) {
        //Shouldn't be a key in both maps
        LOBSummary summary = exchangeSummaries.get(e);

        if (null != summary)
            return Optional.of(summary.getBuyOrder());

        summary = sipSummaries.get(e);
        if (null != summary)
            return Optional.of(summary.getBuyOrder());

        return Optional.empty();
    }

    public Optional<OrderTimeStamped> getBestOffer(NetworkEntity e) {
        //Shouldn't be a key in both maps
        LOBSummary summary = exchangeSummaries.get(e);

        if (null != summary)
            return Optional.of(summary.getSellOrder());

        summary = sipSummaries.get(e);
        if (null != summary)
            return Optional.of(summary.getSellOrder());

        return Optional.empty();
    }
}