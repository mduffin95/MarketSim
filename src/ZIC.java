import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;
    private Order bestOrder;

    public ZIC(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
        super(model, limit, e, sip);
        this.direction = direction;

        int theoretical;
        if (direction == Direction.BUY) {
            theoretical = limit - MarketSimModel.EQUILIBRIUM;
        } else {
            theoretical = MarketSimModel.EQUILIBRIUM - limit;
        }
        if(theoretical > 0) {
            marketSimModel.theoreticalUtility += theoretical;
        }
        sendTraceNote(getName() + " theoretical utility = " + theoretical);
    }

    @Override
    public void doSomething() {
        Order newOrder = getOrder();

        if (bestOrder == null ||
                direction == Direction.BUY && newOrder.getPrice() > bestOrder.getPrice() ||
                direction == Direction.SELL && newOrder.getPrice() < bestOrder.getPrice()) {
            primaryExchange.send(this, MessageType.CANCEL, bestOrder);
            primaryExchange.send(this, MessageType.LIMIT_ORDER, newOrder);
            bestOrder = newOrder;
        }
    }

    @Override
    protected void respond(MarketUpdate update) {
        if (null == update.trade) {return;}

        if (this == update.trade.buyer) {
            assert direction == Direction.BUY;
            this.active = false;
            utility = limit - update.trade.price;

        } else if (this == update.trade.seller) {
            assert direction == Direction.SELL;
            this.active = false;
            utility = update.trade.price - limit;

        } else { //Neither buyer nor seller
            return;
        }

        //Was a buyer or a seller in this trade
        marketSimModel.totalUtility += utility;
        sendTraceNote(getName() + " utility = " + utility);
    }

    private Order getOrder() {
        int price;
        if (direction == Direction.BUY) {
            price = marketSimModel.generator.nextInt(limit + 1);

        } else {
            price = limit + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit + 1);
        }
        return new Order(this, primaryExchange, direction, price);
    }
}
