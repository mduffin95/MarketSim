import desmoj.core.simulator.*;

public abstract class TradingAgent extends NetworkEntity {
    protected boolean finished;
    protected int limit;
    protected int utility;

    protected Exchange primaryExchange;

    protected MarketSimModel marketSimModel;

    public TradingAgent(Model model, int limit) {
        super(model, "TradingAgent", true);
        utility = 0;
        marketSimModel = (MarketSimModel) model;
        this.limit = limit;
        this.finished = false;
    }

    public abstract Payload getPayload();

    public void handlePacket(Packet packet) {
        //TODO: At some point it needs to be able to handle price updates
        throw new UnsupportedOperationException();
    }

    protected void traded(int price, boolean buy) {
        this.finished = true;
        if (buy) {
            utility = limit - price;
        } else {
            utility = price - limit;
        }
        marketSimModel.totalUtility += utility;

        sendTraceNote(getName() + " utility = " + utility);
    }
}
