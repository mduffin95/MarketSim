package com.matt.marketsim.entities.agents;
import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.entities.*;

import desmoj.core.simulator.*;


public abstract class TradingAgent extends NetworkEntity {


    public Direction direction;
    public boolean active;
//    MarketSimModel marketSimModel;
    SimClock clock;
    OrderRouter router;

    public TradingAgent(Model model, OrderRouter router, boolean showInTrace) {
        super(model, "TradingAgent", showInTrace);
        MarketSimModel marketSimModel = (MarketSimModel) model;
        clock = marketSimModel.getExperiment().getSimClock();
        this.active = true;
        this.router = router;
    }
    //Called by the recurring event
    //TODO: What happens if this method is called while we are waiting for a cancel acknowledgement? State machine?
    public abstract void doSomething();


    @Override
    public void onOwnCompleted(MarketUpdate update) {
        router.respond(update);
    }
    @Override
    public void onMarketUpdate(MarketUpdate update) {
        router.respond(update);
    }

    /*
     * These methods aren't necessary for trading agents.
     */
    @Override
    public void onLimitOrder(Order order) {}
    @Override
    public void onMarketOrder(Order order) {}
    @Override
    public void onCancelOrder(Order order) {}
}
