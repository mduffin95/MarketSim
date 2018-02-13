import desmoj.core.simulator.*;

public class TradingAgent extends NetworkEntity {
    private int limit;
    private boolean buy; //Buy or sell
    private int utility;

    protected Exchange primaryExchange;

    private MarketSimModel exchangeModel;

    public TradingAgent(Model model, int limit, boolean buy) {
        super(model, "TradingAgent", true);
        utility = 0;
        exchangeModel = (MarketSimModel) model;
        this.buy = buy;
        this.limit = limit;
    }

    public Order getOrder() {

        Order order;
        if (buy) {
            order = new BuyOrder(exchangeModel, "BuyOrderEvent", true, this, primaryExchange);
            //Buy for limit or less
            order.price = exchangeModel.generator.nextInt(limit + 1);
        } else {
            order = new SellOrder(exchangeModel, "SellOrderEvent", true, this, primaryExchange);
            //Sell for limit or more
            order.price = limit + exchangeModel.generator.nextInt(exchangeModel.MAX_PRICE - limit + 1);
        }
//        order.price = exchangeModel.getLimitPrice();
        order.agent = this;

        return order;
    }

    protected void traded(int price) {
        if (buy) {
            utility = limit - price;
        } else {
            utility = price - limit;
        }
        sendTraceNote(getName() + " utility = " + utility);
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
