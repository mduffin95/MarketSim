package com.matt.marketsim.builders;

import com.matt.marketsim.Direction;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.models.MarketSimModel;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class DifferentDelay implements NetworkBuilder {
    private int num;
    private int min;
    private int step;


    public DifferentDelay(int num, int min, int max) {
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
            DefaultWeightedEdge e = graph.addEdge(agentBuy, exchange);
            graph.addEdge(agentBuy, sip);

            graph.setEdgeWeight(e, 10000);

            //Add sell agent to graph
            graph.addVertex(agentSell);
            graph.addEdge(agentSell, exchange);
            graph.addEdge(agentSell, sip);
        }
        return graph;
    }
}
