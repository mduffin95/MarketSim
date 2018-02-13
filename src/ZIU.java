import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    protected boolean buy;

    public ZIU(Model model, int limit, boolean buy) {
        super(model, limit);
        this.buy = buy;
    }

    @Override
    public Order getOrder() {
        Order order;
        if (buy) {
            order = new BuyOrder(marketSimModel, "BuyOrderEvent", true, this, primaryExchange);
        } else {
            order = new SellOrder(marketSimModel, "SellOrderEvent", true, this, primaryExchange);
        }

        order.price = marketSimModel.getRandomPrice();
        order.agent = this;

        return order;
    }
}
