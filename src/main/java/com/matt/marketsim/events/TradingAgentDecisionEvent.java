package com.matt.marketsim.events;

import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

public class TradingAgentDecisionEvent extends Event<TradingAgent> {
    MarketSimModel marketSimModel;

    public TradingAgentDecisionEvent(Model model, String s, boolean b) {
        super(model, s, b);
        marketSimModel = (MarketSimModel)model;
    }

    @Override
    public void eventRoutine(TradingAgent tradingAgent) {
        if (!tradingAgent.active) {return;} //If it's finished trading then end

        //Runs the trading agent's strategy
        tradingAgent.doSomething();

        //Reschedule
        schedule(tradingAgent, marketSimModel.getAgentArrivalTime());
    }
}
