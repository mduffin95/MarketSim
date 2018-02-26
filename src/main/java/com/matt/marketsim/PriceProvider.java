package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;

public interface PriceProvider {
    void registerPriceObserver(NetworkEntity networkEntity);
}
