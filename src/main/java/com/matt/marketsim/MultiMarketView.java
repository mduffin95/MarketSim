package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import desmoj.core.simulator.TimeInstant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MultiMarketView {
    private Map<Exchange, QuoteData> bidSummaryMap;
    private Map<Exchange, QuoteData> offerSummaryMap;

    public MultiMarketView() {
        bidSummaryMap = new HashMap<>();
        offerSummaryMap = new HashMap<>();
    }

    //TODO: should not be source of update, but exchange within order.
    public void add(MarketUpdate update) {
        if (null == update)
            return;
        LOBSummary summary = update.summary;

        if (null == summary)
            return;

        QuoteData bid = summary.getBuyQuote();
        checkAndUpdate(bid);
        QuoteData offer = summary.getSellQuote();
        checkAndUpdate(offer);
    }

    private void checkAndUpdate(QuoteData newQuote) {
        if (null != newQuote) {
            QuoteData oldQuote;
            Map<Exchange, QuoteData> map;
            if (newQuote.direction == Direction.BUY){
                map = bidSummaryMap;
            } else {
                map = offerSummaryMap;
            }
            oldQuote = map.get(newQuote.exchange);
            if (null == oldQuote) {
                map.put(newQuote.exchange, newQuote);
            } else {
                if (!newQuote.getExchange().equals(oldQuote.getExchange()) || newQuote.moreRecentThan(oldQuote)) {
                    //Either quotes are from different exchanges, or they are from the same exchange and the new one is more recent
                    map.put(newQuote.exchange, newQuote);
                }
            }
        }
    }

    public QuoteData getBestBid() {
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
        return bestBid;
    }

    public QuoteData getBestOffer() {
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
        return bestOffer;
    }

    public QuoteData getBestBid(Exchange e) {
        return bidSummaryMap.get(e);
    }

    public QuoteData getBestOffer(Exchange e) {
        return offerSummaryMap.get(e);
    }
}