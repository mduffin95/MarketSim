package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;
import com.matt.marketsim.entities.Exchange;

public class Order implements Comparable<Order> {
    public TradingAgent agent;
    private Exchange exchange;
    public Direction direction;
    private TimeInstant timeStamp;
    private int price;

    public Order(TradingAgent agent, Exchange exchange, Direction direction, int price) {
        this.agent = agent;
        this.exchange = exchange;
        this.direction = direction;
        this.price = price;
    }

    @Override
    public int compareTo(Order payload) {
        return price - payload.price;
    }

    public void setTimeStamp(TimeInstant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "[com.matt.marketsim.Order - Agent: " + agent.getName() + ", com.matt.marketsim.Direction: " + direction + ", Price: " + price + "]";
    }
}
