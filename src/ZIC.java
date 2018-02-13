import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {

    protected boolean buy;

    public ZIC(Model model, int limit, boolean buy) {
        super(model, limit);
        this.buy = buy;
    }

    @Override
    public Order getOrder() {

        Order order;
        if (buy) {
            order = new BuyOrder(marketSimModel, "BuyOrderEvent", true, this, primaryExchange);
            //Buy for limit or less
            order.price = marketSimModel.generator.nextInt(limit + 1);
        } else {
            order = new SellOrder(marketSimModel, "SellOrderEvent", true, this, primaryExchange);
            //Sell for limit or more
            order.price = limit + marketSimModel.generator.nextInt(marketSimModel.MAX_PRICE - limit + 1);
        }
//        order.price = marketSimModel.getLimitPrice();
        order.agent = this;

        return order;
    }
}
