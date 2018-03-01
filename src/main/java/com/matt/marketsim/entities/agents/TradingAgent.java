package com.matt.marketsim.entities.agents;
import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.entities.*;

import desmoj.core.simulator.*;


public abstract class TradingAgent extends NetworkEntity {


    public Direction direction;
    public boolean active;
    public int limit;
//    protected int utility;

    protected Exchange primaryExchange;
    private SecuritiesInformationProcessor sip;

    protected MarketSimModel marketSimModel;

    public TradingAgent(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip) {
        super(model, "TradingAgent", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        marketSimModel = (MarketSimModel) model;
        marketSimModel.registerForInitialSchedule(this); //Register so that it is scheduled
        this.limit = limit;
        this.active = true;

        this.primaryExchange = e;
        this.primaryExchange.registerPriceObserver(this);

        this.sip = sip;
        this.sip.registerPriceObserver(this); //Will get price updates from SIP

    }
    //Called by the recurring event
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
        if (direction == Direction.BUY)
            return limit - equilibrium;
        else
            return equilibrium - limit;
    }
}
