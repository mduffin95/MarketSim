package com.matt.marketsim.events;

import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Event;

import java.util.Queue;

public class TradingAgentDecisionEvent extends Event<TradingAgent> {
    MarketSimModel marketSimModel;
    Queue<TradingAgent> queue;

    //TODO: Pass in a distribution for sampling a rescheduling time.
    public TradingAgentDecisionEvent(MarketSimModel model, String s, boolean b, Queue<TradingAgent> queue) {
        super(model, s, b);
        marketSimModel = model;
        this.queue = queue;
    }

    @Override
    public void eventRoutine(TradingAgent tradingAgent) {
        if (tradingAgent.active) {
            //Runs the trading agent's strategy
            tradingAgent.doSomething();
            queue.add(tradingAgent); //re-insert
        }
        schedule(queue.remove(), marketSimModel.getAgentArrivalTime()); //schedule next
    }
}
