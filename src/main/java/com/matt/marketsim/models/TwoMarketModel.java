package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import desmoj.core.dist.*;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TwoMarketModel extends MarketSimModel {

    /*
     * Distributions and number generators
     */
    public Random generator;
    private ContDistExponential agentArrivalTime;
    private ContDistUniform agentArrivalTimeUniform;
    public ContDistUniform offsetRange;
    private DiscreteDistUniform priceDist;
    //    private BoolDistBernoulli buyOrSell;
    public DistributionManager distributionManager;

    private SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> network;

    /*
     * Model parameters
     */

    private int OFFSET_RANGE = 40;
    private int MEAN_TIME_BETWEEN_TRADES = 10;
    private boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = true;
    public static boolean PACKET_SEND_IN_TRACE = false;
    public static boolean PACKET_ARRIVAL_IN_TRACE = false;


    /*
     * Wellman's parameters
     */
    private double sigma_shock = 12250;
    private double sigma_pv = 10000;
    private double k = 0.05;
    private double initial_fundamental = 100000;
    private double alpha = 0.001; //Arbitrageur threshold


    public TwoMarketModel(TimeUnit unit, int simLength) {
        super(null, "TwoMarketModel", true, true, unit, simLength);
        generator = new Random();
    }

    @Override
    public String description() {
        return "Market simulator model.";
    }

    @Override
    public void init() {

        /*
         * Distributions and number generators
         */
        long seed = generator.nextLong();
        distributionManager = new DistributionManager("Distribution Manager", seed);
        agentArrivalTime = new ContDistExponential(this, "AgentArrivalTimeStream",
                MEAN_TIME_BETWEEN_TRADES, true, false);
        agentArrivalTime.setNonNegative(true);
        offsetRange = new ContDistUniform(this, "OffsetRangeUniformStream",
                0, OFFSET_RANGE, true, false);

        distributionManager.register(agentArrivalTime);
        distributionManager.register(agentArrivalTimeUniform);
        distributionManager.register(priceDist);
//        distributionManager.register(buyOrSell);


        network = createNetwork();
    }

    @Override
    public TimeSpan getAgentArrivalTime() {
        return new TimeSpan(agentArrivalTime.sample(), timeUnit);
    }

    @Override
    protected SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> getNetwork() {
        return network;
    }

    @Override
    public void setSeed(long s) {
        generator.setSeed(s);
    }


    //Create the network of entities
    private SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork() {
        double discountRate = 0.0006;
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan(3, timeUnit));
        Exchange exchange1 = new Exchange(this, "Exchange1", sip, SHOW_ENTITIES_IN_TRACE);
        Exchange exchange2 = new Exchange(this, "Exchange2", sip, SHOW_ENTITIES_IN_TRACE);


        SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(sip);
        graph.addVertex(exchange1);
        graph.addVertex(exchange2);
        graph.setEdgeWeight(graph.addEdge(exchange1, sip), 0);
        graph.setEdgeWeight(graph.addEdge(exchange2, sip), 0);


        TradingAgentGroup ex1 = new TradingAgentGroup();
        TradingAgentGroup ex2 = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();


        TradeTimeSeries e1TradePrices = new TradeTimeSeries(this, "Exchange 1 trade prices", ex1, "e1_trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(simLength, timeUnit), true, false);

        TradeTimeSeries e2TradePrices = new TradeTimeSeries(this, "Exchange 2 trade prices", ex2, "e2_trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(simLength, timeUnit), true, false);

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(this, "Stats (group 1)", all, discountRate, true, false);

        exchange1.lastTradeSupplier.addObserver(tradeStats);
        exchange1.lastTradeSupplier.addObserver(e1TradePrices);

        exchange2.lastTradeSupplier.addObserver(tradeStats);
        exchange2.lastTradeSupplier.addObserver(e2TradePrices);

        VariableLimitFactory factory = new VariableLimitFactory(this, sigma_shock, sigma_pv, k, initial_fundamental);

        //Arbitrageur
        if (true) {
            TradingAgent arbitrageur = new Arbitrageur(this, alpha);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            TradeStatisticCalculator arbStats = new TradeStatisticCalculator(this, "Stats (arbitrageur)", arb, discountRate, true, false);
            exchange1.lastTradeSupplier.addObserver(arbStats);
            exchange2.lastTradeSupplier.addObserver(arbStats);
            exchange1.registerPriceObserver(arbitrageur);
            exchange2.registerPriceObserver(arbitrageur);

            graph.addVertex(arbitrageur);
            graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange1), 0);
            graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange2),0);
        }

        //Create the supply and demand curves
        Exchange e;
        TradingAgentGroup g;
        SimClock clock = this.getExperiment().getSimClock();
        for (int i = 0; i < 250; i++) {
            OrderRouter r1;
            OrderRouter r2;

            if (i < 125) {
                e = exchange1;
                g = ex1;
            } else {
                e = exchange2;
                g = ex2;
            }
            if ((i%125) < 0) {
                r1 = new FixedOrderRouter(clock, e);
                r2 = new FixedOrderRouter(clock, e);
            } else {
                r1 = new BestPriceOrderRouter(clock, e);
                r2 = new BestPriceOrderRouter(clock, e);
            }
            TradingAgent agent1 = new ZIC(this, factory.create(), r1, Direction.BUY);
            TradingAgent agent2 = new ZIC(this, factory.create(), r2, Direction.SELL);

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
