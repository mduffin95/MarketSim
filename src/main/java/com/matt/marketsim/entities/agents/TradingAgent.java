package com.matt.marketsim.entities.agents;
import com.matt.marketsim.*;
import com.matt.marketsim.LimitProvider;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.entities.*;

import desmoj.core.simulator.*;


public abstract class TradingAgent extends NetworkEntity {


    public Direction direction;
    public boolean active;
    protected LimitProvider limit;
//    protected int utility;

    OrderRouter router;
    MarketSimModel marketSimModel;
    SimClock clock;

    public TradingAgent(Model model, LimitProvider limit, OrderRouter router) {
        super(model, "TradingAgent", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        marketSimModel = (MarketSimModel) model;
        marketSimModel.registerForInitialSchedule(this); //Register so that it is scheduled
        clock = marketSimModel.getExperiment().getSimClock();
        this.limit = limit;
        this.active = true;
        this.router = router;
    }
    //Called by the recurring event
    //TODO: What happens if this method is called while we are waiting for a cancel acknowledgement? State machine?
    public abstract void doSomething();

    protected abstract void respond(MarketUpdate update);
    protected abstract void cancelSuccess(Order order);

    public void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case LIMIT_ORDER:
                break;
            case MARKET_ORDER:
                break;
            case MARKET_UPDATE:
                MarketUpdate update = (MarketUpdate)packet.getPayload();
                if (null != router) {
                    router.respond(update); //Call router first, in case the trading agent responds by sending an order.
                }
                respond(update);
            case CANCEL:
                break;
            case CANCEL_SUCCESS:
                Order order = (Order) packet.getPayload();
                cancelSuccess(order);
                break;
            case CANCEL_FAILURE:
                active = false;
                break;
        }
    }

    boolean isMyTrade(Trade trade) {
        return null != trade && (this == trade.buyer || this == trade.seller);
    }

    /*
     * Handles the utility of a trading agent and sets them to inactive after a successful trade.
     */
    void handleTrade(Trade trade) {
        if (isMyTrade(trade)) {
            //Was a buyer or a seller in this trade
            this.active = false;
        }
    }

    public int getTheoreticalUtility(int equilibrium) {
        if (null == limit) {
            throw new RuntimeException("Tried to get theoretical utility of agent with no limit price.");
        }

        if (direction == Direction.BUY)
            return limit.getLimitPrice(null) - equilibrium;
        else
            return equilibrium - limit.getLimitPrice(null);
    }

    public int getLimitPrice(Order order) {
        return limit.getLimitPrice(order);
    }
}
