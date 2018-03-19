package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.dtos.ResultDto;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import desmoj.core.dist.*;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TwoMarketModel extends MarketSimModel {

    /*
     * Distributions and number generators
     */
    private ContDistExponential agentArrivalTimeDist;
    public ContDistUniform offsetRangeDist;
    private DiscreteDistUniform priceDist;
    private BoolDistBernoulli buyOrSell;
    //    private BoolDistBernoulli buyOrSell;

    /*
     * Model parameters
     */

    private boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = true;
    public static boolean PACKET_SEND_IN_TRACE = false;
    public static boolean PACKET_ARRIVAL_IN_TRACE = false;

    /*
     * Statistics
     */
    List<TradeStatisticCalculator> statsObjects;


    /*
     * Wellman's parameters
     */
    private double SIGMA_SHOCK;
    private double SIGMA_PV;
    private double k;
    private double MEAN_FUNDAMENTAL;
    private double ALPHA; //Arbitrageur threshold
    private double DELTA;
    private double OFFSET_RANGE;
    private double LAMBDA; //Arrival rate
    private int NUM_AGENTS;


    public TwoMarketModel(int simLength, int num_agents, double alpha, double mean_fundamental, double k, double var_pv, double var_shock, double range, double lambda, double delta) {
        super(null, "TwoMarketModel", true, true, simLength);
        generator = new Random();
        this.simLength = simLength;
        this.ALPHA = alpha;
        this.MEAN_FUNDAMENTAL = mean_fundamental;
        this.k = k;
        this.SIGMA_PV = Math.sqrt(var_pv);
        this.SIGMA_SHOCK = Math.sqrt(var_shock);
        this.OFFSET_RANGE = range;
        this.LAMBDA = lambda;
        this.DELTA = delta;
        this.NUM_AGENTS = num_agents;

        statsObjects = new ArrayList<>();
    }

    @Override
    public String description() {
        return "A two market model from the Wah and Wellman paper.";
    }

    @Override
    public void doInitialSchedules() {
        Collections.shuffle(initialAgents, generator);
        double cumulative = 0.0;
        for (TradingAgent a : initialAgents) {
            cumulative += agentArrivalTimeDist.sample();
            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", true, false);
            event.schedule(a, new TimeSpan(cumulative));
        }
    }

    @Override
    public void init() {
        super.init();

        /*
         * Distributions and number generators
         */

        agentArrivalTimeDist = new ContDistExponential(this, "AgentArrivalTimeStream",
                (1.0 / LAMBDA), true, false);
        agentArrivalTimeDist.setNonNegative(true);
        offsetRangeDist = new ContDistUniform(this, "OffsetRangeUniformStream",
                0, OFFSET_RANGE, true, false);

        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        /*
         * Add distributions to the distribution manager. This allows us to set a single seed.
         */
        distributionManager.register(agentArrivalTimeDist);
        distributionManager.register(offsetRangeDist);
        distributionManager.register(buyOrSell);

        network = createNetwork(); //Always call this last
    }

    @Override
    public TimeSpan getAgentArrivalTime() {
        return new TimeSpan(agentArrivalTimeDist.sample());
    }

    @Override
    protected SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> getNetwork() {
        return network;
    }

    @Override
    public boolean showPacketSendInTrace() {
        return PACKET_SEND_IN_TRACE;
    }

    @Override
    public boolean showPacketArrivalInTrace() {
        return PACKET_ARRIVAL_IN_TRACE;
    }


    //Create the network of entities
    @Override
    SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork() {
        double discountRate = 0.0006;
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan(DELTA));
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


        TradeTimeSeries e1TradePrices = new TradeTimeSeries(this, "Exchange 1 trade prices", ex1,
                "e1_trade_prices.txt", new TimeInstant(0.0), new TimeInstant(simLength), true, false);

        TradeTimeSeries e2TradePrices = new TradeTimeSeries(this, "Exchange 2 trade prices", ex2,
                "e2_trade_prices.txt", new TimeInstant(0.0), new TimeInstant(simLength), true, false);

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(this, "trading_agents", all,
                discountRate, getExperiment().getSimClock(), true, false);

        statsObjects.add(tradeStats);

        exchange1.lastTradeSupplier.addObserver(tradeStats);
        exchange1.lastTradeSupplier.addObserver(e1TradePrices);

        exchange2.lastTradeSupplier.addObserver(tradeStats);
        exchange2.lastTradeSupplier.addObserver(e2TradePrices);

        VariableLimitFactory factory = new VariableLimitFactory(this, SIGMA_SHOCK, SIGMA_PV, k, MEAN_FUNDAMENTAL);

        //Arbitrageur
        if (true) {
            TradingAgent arbitrageur = new Arbitrageur(this, ALPHA, SHOW_ENTITIES_IN_TRACE);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            TradeStatisticCalculator arbStats = new TradeStatisticCalculator(this, "arbitrageur",
                    arb, discountRate, getExperiment().getSimClock(), true, false);
            statsObjects.add(arbStats);
            exchange1.lastTradeSupplier.addObserver(arbStats);
            exchange2.lastTradeSupplier.addObserver(arbStats);
            exchange1.registerPriceObserver(arbitrageur);
            exchange2.registerPriceObserver(arbitrageur);

            graph.addVertex(arbitrageur);
            graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange1), 0);
            graph.setEdgeWeight(graph.addEdge(arbitrageur, exchange2), 0);
        }

        //Create the supply and demand curves
        Exchange e;
        TradingAgentGroup g1;
        TradingAgentGroup g2;
        SimClock clock = this.getExperiment().getSimClock();
        for (int i = 0; i < (NUM_AGENTS / 2); i++) {

            OrderRouter r1 = new BestPriceOrderRouter(clock, exchange1);
            OrderRouter r2 = new BestPriceOrderRouter(clock, exchange2);

            //Market 1
            TradingAgent agent1 = new ZIC(this, factory.create(), r1, buyOrSell, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);
            //Market 2
            TradingAgent agent2 = new ZIC(this, factory.create(), r2, buyOrSell, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);

            //So that they receive price updates
            exchange1.registerPriceObserver(agent1);
            exchange2.registerPriceObserver(agent2);
            sip.registerPriceObserver(agent1);
            sip.registerPriceObserver(agent2);

            //Add to reporting groups
            ex1.addMember(agent1);
            ex2.addMember(agent2);
            all.addMember(agent1);
            all.addMember(agent2);

            //Add buy agent to graph
            graph.addVertex(agent1);
            graph.setEdgeWeight(graph.addEdge(agent1, exchange1), 0);
            graph.setEdgeWeight(graph.addEdge(agent1, exchange2), 0);
            graph.setEdgeWeight(graph.addEdge(agent1, sip), 0);

            //Add sell agent to graph
            graph.addVertex(agent2);
            graph.setEdgeWeight(graph.addEdge(agent2, exchange1), 0);
            graph.setEdgeWeight(graph.addEdge(agent2, exchange2), 0);
            graph.setEdgeWeight(graph.addEdge(agent2, sip), 0);

        }
        return graph;
    }

    @Override
    public void writeResultsToFile(Path path) {
        Iterator<TradeStatisticCalculator> iter = statsObjects.iterator();
        String toWrite = String.valueOf(DELTA) + ", ";
        while (iter.hasNext()) {
            TradeStatisticCalculator c = iter.next();

            toWrite += String.valueOf(c.getTotalUtility());
            if (iter.hasNext()) {
                toWrite = toWrite + ", ";
            }
        }
        try {
            Files.write(path, Arrays.asList(toWrite), Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {

        }
    }

    @Override
    public ResultDto getResults() {
        ResultDto result = new ResultDto();
        result.delta = DELTA;
        for (TradeStatisticCalculator c: statsObjects) {
            result.tradeStatisticDtos.add(c.getResults());
        }
        return result;
    }
}
