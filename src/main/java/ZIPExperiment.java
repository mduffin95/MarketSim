import java.util.ArrayList;

public class ZIPExperiment implements NetworkBuilder {
    private int num;
    private int min;
    private int step;


    public ZIPExperiment(int num, int min, int max) {
        assert num > 0;
        assert max > min;
        this.num = num;
        this.min = min;
        int diff = max - min;
        step = diff / num;
    }

    @Override
    public void createNetworkEntities(MarketSimModel model, ArrayList<TradingAgent> tradingAgents, ArrayList<Exchange> exchanges, SecuritiesInformationProcessor sip) {
        sip = new SecuritiesInformationProcessor(model, "Securities Information Processor", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        Exchange exchange = new Exchange(model, "Exchange", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        exchanges.add(exchange);

        //Create the supply and demand curves
        for (int i = 0; i < num; i++) {
            TradingAgent agentBuy = new ZIP(model, min + i * step, exchange, sip, Direction.BUY);
            TradingAgent agentSell = new ZIP(model, min + i * step, exchange, sip, Direction.SELL);

            tradingAgents.add(agentBuy);
            tradingAgents.add(agentSell);
        }
    }
}
