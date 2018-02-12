import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.simulator.*;

public class TradingAgent extends Entity {
    private int limit;
    private boolean buy; //Buy or sell
    private int utility;

    ExchangeModel exchangeModel;

    public TradingAgent(Model model) {
        super(model, "TradingAgent", true);
        utility = 0;
        exchangeModel = (ExchangeModel) model;
        buy = exchangeModel.getBuyOrSell();
        limit = exchangeModel.getLimitPrice();

    }

    protected Order getOrder() {

        Order order;
        if (buy) {
            order = new BuyOrder(exchangeModel, "BuyOrderEvent", true);
        } else {
            order = new SellOrder(exchangeModel, "SellOrderEvent", true);
        }

        order.price = limit;
        order.ta = this;

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

}
