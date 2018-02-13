import desmoj.core.simulator.*;

public abstract class TradingAgent extends NetworkEntity {

    public boolean traded = false; //TODO: remove this

    protected int limit;
    protected int utility;

    protected Exchange primaryExchange;

    protected MarketSimModel marketSimModel;

    public TradingAgent(Model model, int limit) {
        super(model, "TradingAgent", true);
        utility = 0;
        marketSimModel = (MarketSimModel) model;
        this.limit = limit;
    }

    public abstract Order getOrder();

    protected void traded(int price, boolean buy) {
        traded = true;
        int theoretical;
        if (buy) {
            utility = limit - price;
            theoretical = limit - MarketSimModel.EQUILIBRIUM;
        } else {
            utility = price - limit;
            theoretical = MarketSimModel.EQUILIBRIUM - limit;
        }
        marketSimModel.totalUtility += utility;
        if(theoretical > 0) {
            marketSimModel.theoreticalUtility += theoretical;
        }
        sendTraceNote(getName() + " utility = " + utility);
        sendTraceNote(getName() + " theoretical utility = " + theoretical);
    }

    @Override
    public void handleSellOrder(SellOrder sellOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleBuyOrder(BuyOrder buyOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        throw new UnsupportedOperationException();
    }
}
