import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private Direction direction;

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
        //this sends a packet immediately
        primaryExchange.send(this, MessageType.LIMIT_ORDER, getPayload());
    }

    @Override
    public Object getPayload() {
        int price;
        MessageType type;
        if (direction == Direction.BUY) {
            price = marketSimModel.generator.nextInt(limit + 1);

        } else {
            price = limit + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit + 1);
        }
        return new Order(this, primaryExchange, direction, price);
    }
}
