package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.TimeInstant;
import com.matt.marketsim.entities.Exchange;

public class Order implements Comparable<Order> {
    public TradingAgent agent;
    private Exchange exchange;
    public Direction direction;

    private TimeInstant timeStamp;

    public TimeInstant getTimeStamp() {
        return timeStamp;
    }

    private int price;
    private int limit; //For statistics

    public Order(TradingAgent agent, Exchange exchange, Direction direction, int price, int limit, TimeInstant time) {
        this.agent = agent;
        this.exchange = exchange;
        this.direction = direction;
        this.price = price;
        this.limit = limit;

        timeStamp = time;
    }

    @Override
    public int compareTo(Order payload) {
        return price - payload.price;
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

    @Override
    public String toString() {
        return "[com.matt.marketsim.Order - Agent: " + agent.getName() + ", com.matt.marketsim.Direction: " + direction + ", Price: " + price + "]";
    }


}
