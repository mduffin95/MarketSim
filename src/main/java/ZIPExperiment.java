import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

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
    public SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model) {
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(model, "Securities Information Processor", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        Exchange exchange = new Exchange(model, "Exchange", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);


        SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(sip);
        graph.addVertex(exchange);
        graph.addEdge(exchange, sip);

        //Create the supply and demand curves
        for (int i = 0; i < num; i++) {
            TradingAgent agentBuy = new ZIP(model, min + i * step, exchange, sip, Direction.BUY);
            TradingAgent agentSell = new ZIP(model, min + i * step, exchange, sip, Direction.SELL);

            //Add buy agent to graph
            graph.addVertex(agentBuy);
            graph.addEdge(agentBuy, exchange);
            graph.addEdge(agentBuy, sip);

            //Add sell agent to graph
            graph.addVertex(agentSell);
            graph.addEdge(agentSell, exchange);
            graph.addEdge(agentSell, sip);
        }
        return graph;
    }
}
