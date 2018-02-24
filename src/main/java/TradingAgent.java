

import desmoj.core.simulator.*;

public abstract class TradingAgent extends NetworkEntity {
    protected boolean active;
    protected int limit;
    protected int utility;

    protected Exchange primaryExchange;
    private SecuritiesInformationProcessor sip;

    protected MarketSimModel marketSimModel;

    public TradingAgent(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip) {
        super(model, "main.java.TradingAgent", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        utility = 0;
        marketSimModel = (MarketSimModel) model;
        this.limit = limit;
        this.active = true;

        this.primaryExchange = e;
        this.primaryExchange.registerPriceObserver(this);

        this.sip = sip;
        this.sip.registerPriceObserver(this);
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
            if (this == trade.buyer) {
                utility = limit - trade.price;
            } else if (this == trade.seller) {
                utility = trade.price - limit;
            }
            //Was a buyer or a seller in this trade
            this.active = false;
            marketSimModel.totalUtility += utility;
            sendTraceNote(getName() + " utility = " + utility);
        }
    }
}
