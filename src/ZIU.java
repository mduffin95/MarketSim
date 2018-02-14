import desmoj.core.simulator.Model;

public class ZIU extends TradingAgent {
    private boolean buy;

    public ZIU(Model model, int limit, boolean buy) {
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
    public Packet getPacket() {
        Packet packet;
        Payload payload = new Payload();
        if (buy) {
            payload.type = MessageType.BUYORDER;
            packet = new Packet(marketSimModel, "BuyOrderEvent", true, this, primaryExchange, payload);
        } else {
            payload.type = MessageType.SELLORDER;
            packet = new Packet(marketSimModel, "SellOrderEvent", true, this, primaryExchange, payload);
        }

        payload.price = marketSimModel.getRandomPrice();
        payload.agent = this;
        return packet;
    }
}
