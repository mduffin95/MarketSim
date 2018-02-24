

import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private Direction direction;

    public ZIU(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
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
        int price = marketSimModel.getRandomPrice();
        Order order = new Order(this, primaryExchange, direction, price);

        //this sends a packet immediately
        primaryExchange.send(this, MessageType.LIMIT_ORDER, order);
    }

    @Override
    protected void respond(MarketUpdate update) {
        return;
    }
}
