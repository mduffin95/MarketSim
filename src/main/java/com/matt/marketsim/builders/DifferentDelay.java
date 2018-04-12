//package com.matt.marketsim.builders;
//
//import com.matt.marketsim.*;
//import com.matt.marketsim.entities.Exchange;
//import com.matt.marketsim.entities.NetworkEntity;
//import com.matt.marketsim.entities.SecuritiesInformationProcessor;
//import com.matt.marketsim.entities.agents.TradingAgent;
//import com.matt.marketsim.entities.agents.ZIP;
//import com.matt.marketsim.models.MarketSimModel;
//import desmoj.core.simulator.SimClock;
//import desmoj.core.simulator.TimeInstant;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import org.jgrapht.graph.SimpleWeightedGraph;
//
//public class DifferentDelay implements NetworkBuilder {
//    private int num;
//    private int min;
//    private int step;
//    private Schedule schedule;
//
//    public DifferentDelay(int num, int min, int max) {
//        assert num > 0;
//        assert max > min;
//        this.num = num;
//        this.min = min;
//        int diff = max - min;
//        step = diff / (num-1);
//        schedule = new Schedule(min, step, num, min, step, num);
//    }
//
//    @Override
//    public SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model) {
//        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(model, "Securities Information Processor", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
//        Exchange exchange = new Exchange(model, "Exchange", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);
//
//        SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
//        graph.addVertex(sip);
//        graph.addVertex(exchange);
//        graph.addEdge(exchange, sip);
//
//
//        FixedLimit[] buySchedule = schedule.getBuySchedule();
//        FixedLimit[] sellSchedule = schedule.getSellSchedule();
//        int equilibrium = schedule.getEquilibriumPrice();
//        System.out.println("Equilibrium = " + equilibrium);
//
//        TradingAgentGroup shortDelay = new TradingAgentGroup(equilibrium);
//        TradingAgentGroup longDelay = new TradingAgentGroup(equilibrium);
//        TradingAgentGroup all = new TradingAgentGroup();
//
//        TradeTimeSeries tradePrices = new TradeTimeSeries(model, "Trade prices over time", all, "trade_prices.txt",
//                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);
//        TradeStatisticCalculator tradeStatShort = new TradeStatisticCalculator(model, "Stats (short delay)", shortDelay, equilibrium, true, false);
//        TradeStatisticCalculator tradeStatLong = new TradeStatisticCalculator(model, "Stats (long delay)", longDelay, equilibrium, true, false);
//
//        exchange.registerLastTradeObserver(tradePrices);
//        exchange.registerLastTradeObserver(tradeStatShort);
//        exchange.registerLastTradeObserver(tradeStatLong);
//        SimClock clock = model.getExperiment().getSimClock();
//        //Create the supply and demand curves
//        for (int i = 0; i < num; i++) {
//            TradingAgent agentBuy = new ZIP(model, buySchedule[i].getLimitPrice(null), new FixedOrderRouter(clock, exchange), Direction.BUY);
//            TradingAgent agentSell = new ZIP(model, sellSchedule[i].getLimitPrice(null), new FixedOrderRouter(clock, exchange), Direction.SELL);
//
//            //So that they receive price updates
//            exchange.registerPriceObserver(agentBuy);
//            exchange.registerPriceObserver(agentSell);
//            sip.registerPriceObserver(agentBuy);
//            sip.registerPriceObserver(agentSell);
//
//            //Add buy agent to graph
//            graph.addVertex(agentBuy);
//            DefaultWeightedEdge e1 = graph.addEdge(agentBuy, exchange);
//            graph.addEdge(agentBuy, sip);
//
//            //Add sell agent to graph
//            graph.addVertex(agentSell);
//            DefaultWeightedEdge e2 = graph.addEdge(agentSell, exchange);
//            graph.addEdge(agentSell, sip);
//
//            if ((i % 2) == 0) {
//                graph.setEdgeWeight(e1, 10000);
//                graph.setEdgeWeight(e2, 1);
//
//                shortDelay.addMember(agentSell);
//                longDelay.addMember(agentBuy);
//            }
//            else {
//                graph.setEdgeWeight(e1, 1);
//                graph.setEdgeWeight(e2, 10000);
//
//                shortDelay.addMember(agentBuy);
//                longDelay.addMember(agentSell);
//            }
//            all.addMember(agentBuy);
//            all.addMember(agentSell);
//
//        }
//        return graph;
//    }
//}
