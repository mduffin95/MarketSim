import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private boolean buy;

    public ZIU(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, boolean buy) {
        super(model, limit, e, sip);
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
            payload.type = MessageType.BUYORDER;
        } else {
            payload.type = MessageType.SELLORDER;
        }

        payload.price = marketSimModel.getRandomPrice();
        payload.agent = this;
        return payload;
    }
}
