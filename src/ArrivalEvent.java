import desmoj.core.simulator.*;

public class ArrivalEvent extends Event<TradingAgent> {

    // define attributes here (optional)
    private ExchangeModel exchangeModel;

    /**
     * constructs an event...
     */
    public ArrivalEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);

        exchangeModel = (ExchangeModel) model;
    }

    /**
     * Get an order from the trading agent and submit it to the exchange
     */
    public void eventRoutine(TradingAgent agent) {
        Order order = agent.getOrder();
        exchangeModel.submitOrder(order);

        sendTraceNote("BuyQueueLength: " + exchangeModel.buyQueue.length());
        sendTraceNote("SellQueueLength: " + exchangeModel.sellQueue.length());
    }

    // define additional methods here (optional)

} /* end of event class */
