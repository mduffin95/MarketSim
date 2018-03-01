package com.matt.marketsim.entities.agents;

import com.matt.marketsim.*;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private Direction direction;

    public ZIU(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
        super(model, limit, e, sip);
        this.direction = direction;

    }

    @Override
    public void doSomething() {
        int price = marketSimModel.getRandomPrice();
        Order order = new Order(this, primaryExchange, direction, price);

        //this sends a packet immediately
        primaryExchange.send(this, MessageType.LIMIT_ORDER, order);
    }

    @Override
    protected void respond(MarketUpdate update) {
        return;
    }

    @Override
    protected void cancelSuccess(Order order) {
        throw new UnsupportedOperationException();
    }

}
