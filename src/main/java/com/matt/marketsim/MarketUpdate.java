package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;

public class MarketUpdate {
    public NetworkEntity source;
    public Trade trade;
    public LOBSummary summary;

    public MarketUpdate(NetworkEntity source, Trade trade, LOBSummary summary) {
        this.source = source;
        this.trade = trade;
        this.summary = summary;
    }
}
