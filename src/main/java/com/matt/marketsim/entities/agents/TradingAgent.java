package com.matt.marketsim.entities.agents;
import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.entities.*;

import desmoj.core.simulator.*;


public abstract class TradingAgent extends NetworkEntity {


    private int group;
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

        this.group = -1;
    }

    public abstract int getTheoreticalUtility(int equilibrium);

    public void setGroup(int group) {
        this.group = group;
    }
    public int getGroup() {
        return group;
    }
    //Called by the recurring event
    public abstract void doSomething();

    protected abstract void respond(MarketUpdate update);

    public void handlePacket(Packet packet) {
        MarketUpdate update;
        switch (packet.getType()) {
            case LIMIT_ORDER:
                break;
            case MARKET_ORDER:
                break;
            case MARKET_UPDATE:
                update = (MarketUpdate)packet.getPayload();
                respond(update);
            case CANCEL:
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
}
