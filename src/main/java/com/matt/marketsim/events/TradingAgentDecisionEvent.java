package com.matt.marketsim.events;

import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

public class TradingAgentDecisionEvent extends Event<TradingAgent> {
    MarketSimModel marketSimModel;
    boolean canReschedule;

    //TODO: Pass in a distribution for sampling a rescheduling time.
    public TradingAgentDecisionEvent(MarketSimModel model, String s, boolean b, boolean canReschedule) {
        super(model, s, b);
        marketSimModel = model;
        this.canReschedule = canReschedule;
    }

    @Override
    public void eventRoutine(TradingAgent tradingAgent) {
        if (!tradingAgent.active) {return;} //If it's finished trading then end

        //Runs the trading agent's strategy
        tradingAgent.doSomething();

        //Reschedule
        if (canReschedule)
            reSchedule(marketSimModel.getAgentArrivalTime());
    }
}
