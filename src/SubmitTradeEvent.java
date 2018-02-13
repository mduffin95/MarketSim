import desmoj.core.simulator.*;

import java.util.concurrent.TimeUnit;

public class SubmitTradeEvent extends Event<TradingAgent> {
    public SubmitTradeEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

    }

    @Override
    public void eventRoutine(TradingAgent tradingAgent) {
        MarketSimModel model = (MarketSimModel) getModel();

        //Get an order from the trading agent and send it to the exchange
        Order order = tradingAgent.getOrder();
        order.send();

        //Schedule generator again
        schedule(tradingAgent, new TimeSpan(model.getAgentArrivalTime(), TimeUnit.SECONDS));
    }
}
