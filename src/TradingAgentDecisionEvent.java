import desmoj.core.simulator.*;

public class TradingAgentDecisionEvent extends Event<TradingAgent> {
    MarketSimModel marketSimModel;

    public TradingAgentDecisionEvent(Model model, String s, boolean b) {
        super(model, s, b);
        marketSimModel = (MarketSimModel)model;
    }

    @Override
    public void eventRoutine(TradingAgent tradingAgent) {
        if (tradingAgent.finished) {return;} //If it's finished trading then end

        //this sends a packet immediately
        tradingAgent.sendPacket();

        //Reschedule
        schedule(tradingAgent, marketSimModel.getAgentArrivalTime());
    }
}
