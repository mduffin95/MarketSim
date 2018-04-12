//package com.matt.marketsim.builders;
//
//import com.matt.marketsim.*;
//import com.matt.marketsim.entities.agents.TradingAgent;
//import com.matt.marketsim.entities.agents.ZIP;
//import com.matt.marketsim.entities.Exchange;
//import com.matt.marketsim.entities.NetworkEntity;
//import com.matt.marketsim.entities.SecuritiesInformationProcessor;
//import com.matt.marketsim.models.MarketSimModel;
//import desmoj.core.simulator.SimClock;
//import desmoj.core.simulator.TimeInstant;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import org.jgrapht.graph.SimpleWeightedGraph;
//
//public class ZIPExperiment implements NetworkBuilder {
//    private int num;
//    private int min;
//    private int step;
//
//
//    public ZIPExperiment(int num, int min, int max) {
//        assert num > 0;
//        assert max > min;
//        this.num = num;
//        this.min = min;
//        int diff = max - min;
//        step = diff / num;
//    }
//
//    @Override
//    public SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model) {
//        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(model, "Securities Information Processor", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
//        Exchange exchange = new Exchange(model, "Exchange", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);
//
//        TradingAgentGroup all = new TradingAgentGroup();
//        TradeTimeSeries tradePrices = new TradeTimeSeries(model, "Trade prices over time", all,"trade_prices.txt",
//                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);
//
//        exchange.registerLastTradeObserver(tradePrices);
//
//        SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
//        graph.addVertex(sip);
//        graph.addVertex(exchange);
//        graph.setEdgeWeight(graph.addEdge(exchange, sip), 0);
//
//        SimClock clock = model.getExperiment().getSimClock();
//        //Create the supply and demand curves
//        for (int i = 0; i < num; i++) {
//            TradingAgent agentBuy = new ZIP(model, min + i * step, new FixedOrderRouter(clock, exchange), Direction.BUY);
//            TradingAgent agentSell = new ZIP(model, min + i * step, new FixedOrderRouter(clock, exchange), Direction.SELL);
//
//            //Add to reporting groups
//            all.addMember(agentBuy);
//            all.addMember(agentSell);
//
//            //Add buy agent to graph
//            graph.addVertex(agentBuy);
//            graph.setEdgeWeight(graph.addEdge(agentBuy, exchange), 0);
//            graph.setEdgeWeight(graph.addEdge(agentBuy, sip), 0);
//
//            //Add sell agent to graph
//            graph.addVertex(agentSell);
//            graph.setEdgeWeight(graph.addEdge(agentSell, exchange), 0);
//            graph.setEdgeWeight(graph.addEdge(agentSell, sip),0);
//        }
//        return graph;
//    }
//}
