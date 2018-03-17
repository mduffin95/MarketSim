package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;

public interface IOrder extends Comparable<IOrder> {
    Exchange getExchange();
    int getPrice();
    int getLimit();
    TimeInstant getTimeStamp();
    TradingAgent getAgent();
    Direction getDirection();
}
