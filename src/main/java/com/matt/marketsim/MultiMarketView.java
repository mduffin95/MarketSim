package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import desmoj.core.simulator.TimeInstant;

import java.util.*;

public class MultiMarketView {
    private Map<Exchange, QuoteData> bidSummaryMap;
    private Map<Exchange, QuoteData> offerSummaryMap;

    public MultiMarketView() {
        bidSummaryMap = new HashMap<>();
        offerSummaryMap = new HashMap<>();
    }

    //TODO: should not be source of update, but exchange within order.
    public void add(MarketUpdate update) {
        Objects.requireNonNull(update, "MarketUpdate can't be null.");
        LOBSummary summary = update.summary;
        Objects.requireNonNull(summary);

        Optional<QuoteData> bid = summary.getBuyQuote();
        if (bid.isPresent()) {
            checkAndUpdate(bid.get(), bidSummaryMap);
        } else {
            //TODO: issue here because we have no reference to the exchange.
            //QuoteData could contain another object to represent a price quote. This could be nullable.
        }
        bid.ifPresent(x -> );
        Optional<QuoteData> offer = summary.getSellQuote();
        offer.ifPresent(x -> checkAndUpdate(x, offerSummaryMap));
    }

    private void checkAndUpdate(QuoteData newQuote, Map<Exchange, QuoteData> map) {
        QuoteData oldQuote = map.get(newQuote.exchange);
        if (null == oldQuote) {
            map.put(newQuote.exchange, newQuote);
        } else if (newQuote.moreRecentThan(oldQuote)) {
            //New quote is more recent
            map.put(newQuote.exchange, newQuote);
        }
    }

    public Optional<QuoteData> getBestBid() {
        QuoteData bestBid = null;
        for (Map.Entry<Exchange, QuoteData> entry : bidSummaryMap.entrySet()) {
            QuoteData bid = entry.getValue();
            if (null == bid) {
                continue;
            }
            if (null == bestBid || bid.getPrice() > bestBid.getPrice()) {
                bestBid = bid;
            }
        }
        return Optional.ofNullable(bestBid);
    }

    public Optional<QuoteData> getBestOffer() {
        QuoteData bestOffer = null;
        for (Map.Entry<Exchange, QuoteData> entry : offerSummaryMap.entrySet()) {
            QuoteData offer = entry.getValue();
            if (null == offer) {
                continue;
            }
            if (null == bestOffer || offer.getPrice() < bestOffer.getPrice()) {
                bestOffer = offer;
            }
        }
        return Optional.ofNullable(bestOffer);
    }

    public Optional<QuoteData> getBestBid(Exchange e) {
        return Optional.ofNullable(bidSummaryMap.get(e));
    }

    public Optional<QuoteData> getBestOffer(Exchange e) {
        return Optional.ofNullable(offerSummaryMap.get(e));
    }
}