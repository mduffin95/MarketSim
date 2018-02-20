import desmoj.core.simulator.*;

public abstract class TradingAgent extends NetworkEntity {
    protected boolean finished;
    protected int limit;
    private int utility;

    protected Exchange primaryExchange;
    private SecuritiesInformationProcessor sip;

    protected MarketSimModel marketSimModel;

    public TradingAgent(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip) {
        super(model, "TradingAgent", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        utility = 0;
        marketSimModel = (MarketSimModel) model;
        this.limit = limit;
        this.finished = false;

        this.primaryExchange = e;
        this.primaryExchange.registerPriceObserver(this);

        this.sip = sip;
        this.sip.registerPriceObserver(this);
    }

    //Called by the reoccurring event
    public abstract void doSomething();

    public void handlePacket(Packet packet) {
        //TODO: At some point it needs to be able to handle price updates
        throw new UnsupportedOperationException();
    }

    protected void traded(int price, Direction direction) {
        this.finished = true;
        if (direction == Direction.BUY) {
            utility = limit - price;
        } else {
            utility = price - limit;
        }
        marketSimModel.totalUtility += utility;

        sendTraceNote(getName() + " utility = " + utility);
    }
}
