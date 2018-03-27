package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;

import java.util.Objects;
import java.util.Optional;

public class MarketUpdate {
    private NetworkEntity source;
    private Trade trade; //Optional
    private LOBSummary summary;

    public MarketUpdate(NetworkEntity source, Trade trade, LOBSummary summary) {
        this.source = Objects.requireNonNull(source, "The source must not be null.");
        this.trade = trade;
        this.summary = Objects.requireNonNull(summary, "The summary must not be null.");
    }

    public NetworkEntity getSource() {
        return source;
    }

    public Optional<Trade> getTrade() {
        return Optional.ofNullable(trade);
    }

    public LOBSummary getSummary() {
        return summary;
    }
}
