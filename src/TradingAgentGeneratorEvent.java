import desmoj.core.simulator.*;

import java.util.concurrent.TimeUnit;

public class TradingAgentGeneratorEvent extends ExternalEvent {

    public TradingAgentGeneratorEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

    }

    @Override
    public void eventRoutine() {
        MarketSimModel model = (MarketSimModel) getModel();

        TradingAgent agent = new TradingAgent(model);
        model.exchange.registerPrimary(agent);

        //Get an order from the trading agent and send it to the exchange
        Order order = agent.getOrder();
        order.send();

        //Schedule generator again
        schedule(new TimeSpan(model.getAgentArrivalTime(), TimeUnit.SECONDS));
    }
}
