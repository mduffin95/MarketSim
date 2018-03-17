package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;
import com.matt.marketsim.entities.Exchange;

public class Order implements IOrder {

    private TradingAgent agent;
    private Exchange exchange;
    public Direction direction;

    private TimeInstant timeStamp;

    public TimeInstant getTimeStamp() {
        return timeStamp;
    }

    private int price;
    private int limit; //For statistics. The limit at the time the order was made.
    //TODO: Remove this limit. Perhaps store a log of orders and limits with the trading agent, or on the statistic calculator.

    public Order(TradingAgent agent, Exchange exchange, Direction direction, int price, int limit, TimeInstant time) {
        this.agent = agent;
        this.exchange = exchange;
        this.direction = direction;
        this.price = price;
        this.limit = limit;

        timeStamp = time;
    }

    @Override
    public int compareTo(IOrder payload) {
        return price - payload.getPrice();
    }

    public Exchange getExchange() {
        return exchange;
    }

    public int getPrice() {
        return price;
    }

    public int getLimit() {
        return limit;
    }

    public TradingAgent getAgent() {
        return agent;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "[com.matt.marketsim.Order - Agent: " + agent.getName() + ", com.matt.marketsim.Direction: " + direction + ", Price: " + price + "]";
    }


}
