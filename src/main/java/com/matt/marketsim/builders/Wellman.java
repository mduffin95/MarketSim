package com.matt.marketsim.builders;

import com.matt.marketsim.*;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.concurrent.TimeUnit;

public class Wellman implements NetworkBuilder {

    @Override
    public SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model) {
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(model, "Securities Information Processor",
                MarketSimModel.SHOW_ENTITIES_IN_TRACE, new TimeSpan(100, TimeUnit.MICROSECONDS));
        Exchange exchange1 = new Exchange(model, "Exchange1", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        Exchange exchange2 = new Exchange(model, "Exchange2", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);


        SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(sip);
        graph.addVertex(exchange1);
        graph.addVertex(exchange2);
        graph.addEdge(exchange1, sip);
        graph.addEdge(exchange2, sip);


        TradingAgentGroup ex1 = new TradingAgentGroup();
        TradingAgentGroup ex2 = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();
        TradingAgentGroup arb = new TradingAgentGroup();
//        TradingAgentGroup arb = new TradingAgentGroup();

        TradeTimeSeries e1TradePrices = new TradeTimeSeries(model, "Exchange 1 trade prices", ex1, "e1_trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);

        TradeTimeSeries e2TradePrices = new TradeTimeSeries(model, "Exchange 2 trade prices", ex2, "e2_trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(model, "Stats (group 1)", all, true, false);

        exchange1.lastTradeSupplier.addObserver(tradeStats);
        exchange1.lastTradeSupplier.addObserver(e1TradePrices);

        exchange2.lastTradeSupplier.addObserver(tradeStats);
        exchange2.lastTradeSupplier.addObserver(e2TradePrices);

        VariableLimit.init(model, 6, 2.5, 0.4, 100);

        //Arbitrageur
        TradingAgent arbitrageur = new Arbitrageur(model);
        arb.addMember(arbitrageur);
        TradeStatisticCalculator arbStats = new TradeStatisticCalculator(model, "Stats (arbitrageur)", arb, true, false);
        exchange1.registerPriceObserver(arbitrageur);
        exchange2.registerPriceObserver(arbitrageur);

        graph.addVertex(arbitrageur);
        graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange1), 0);
        graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange2),0);

        //Create the supply and demand curves
        Exchange e;
        TradingAgentGroup g;
        for (int i = 0; i < 100; i++) {
            if (i < 50) {
                e = exchange1;
                g = ex1;
            } else {
                e = exchange2;
                g = ex2;
            }
            TradingAgent agent1 = new ZIC(model, new VariableLimit(), new BestPriceOrderRouter(e), Direction.BUY);
            TradingAgent agent2 = new ZIC(model, new VariableLimit(), new BestPriceOrderRouter(e), Direction.SELL);

            //So that they receive price updates
            e.registerPriceObserver(agent1);
            e.registerPriceObserver(agent2);
            sip.registerPriceObserver(agent1);
            sip.registerPriceObserver(agent2);

            //Add to reporting groups
            g.addMember(agent1);
            g.addMember(agent2);
            all.addMember(agent1);
            all.addMember(agent2);

            //Add buy agent to graph
            graph.addVertex(agent1);
            graph.setEdgeWeight(graph.addEdge(agent1, exchange1), 0);
            graph.setEdgeWeight(graph.addEdge(agent1, exchange2), 0);
            graph.setEdgeWeight(graph.addEdge(agent1, sip),0);

            //Add sell agent to graph
            graph.addVertex(agent2);
            graph.setEdgeWeight(graph.addEdge(agent2, exchange1),0);
            graph.setEdgeWeight(graph.addEdge(agent2, exchange2),0);
            graph.setEdgeWeight(graph.addEdge(agent2, sip),0);

        }
        return graph;
    }
}
