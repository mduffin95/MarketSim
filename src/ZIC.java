import desmoj.core.simulator.Model;

public class ZIC extends TradingAgent {
    private boolean buy;

    public ZIC(Model model, int limit, boolean buy) {
        super(model, limit);
        this.buy = buy;

        int theoretical;
        if (buy) {
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
    public Payload getPayload() {
        Payload payload = new Payload();
        if (buy) {
            payload.price = marketSimModel.generator.nextInt(limit + 1);
            payload.type = MessageType.BUYORDER;
        } else {
            payload.price = limit + marketSimModel.generator.nextInt(MarketSimModel.MAX_PRICE - limit + 1);
            payload.type = MessageType.SELLORDER;
            //Sell for limit or more

        }

        payload.agent = this;
        return payload;
    }
}
