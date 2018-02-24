

public class ZIPSchedule implements Schedule {
    private int num;
    private int min;
    private int step;


    public ZIPSchedule(int num, int min, int max) {
        assert num > 0;
        assert max > min;
        this.num = num;
        this.min = min;
        int diff = max - min;
        step = diff / num;
    }

    @Override
    public void createAgents(MarketSimModel model, Exchange exchange, SecuritiesInformationProcessor sip) {
        //Create the supply and demand curves
        for (int i = 0; i < num; i++) {
            TradingAgent agentBuy = new ZIP(model, min + i * step, exchange, sip, Direction.BUY);
            TradingAgent agentSell = new ZIP(model, min + i * step, exchange, sip, Direction.SELL);

            TradingAgentDecisionEvent buy = new TradingAgentDecisionEvent(model, "BuyDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);
            TradingAgentDecisionEvent sell = new TradingAgentDecisionEvent(model, "SellDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);

            buy.schedule(agentBuy, model.getAgentArrivalTime());
            sell.schedule(agentSell, model.getAgentArrivalTime());
        }
    }
}
