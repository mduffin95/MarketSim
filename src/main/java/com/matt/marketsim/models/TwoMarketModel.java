package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.dtos.ResultDto;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import desmoj.core.dist.*;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

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
    private double DISCOUNT_RATE;
    private int NUM_AGENTS;


    public TwoMarketModel(int simLength, int num_agents, double alpha, double mean_fundamental, double k, double var_pv, double var_shock, double range, double lambda, double discountRate, double delta) {
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
        this.DISCOUNT_RATE = discountRate;

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
    protected WellmanGraph getNetwork() {
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
    WellmanGraph createNetwork() {
        int numExchanges = 2;
        boolean la_present = true;
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan(DELTA));
        Set<Exchange> allExchanges = new HashSet<>();
        Set<TradingAgent> allTradingAgents = new HashSet<>();
        List<TradingAgentGroup> allExchangeGroups = new ArrayList<>();

        TradingAgentGroup tas = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(this, "trading_agents", tas,
                DISCOUNT_RATE, getExperiment().getSimClock(), true, false);
        statsObjects.add(tradeStats);

        TradingAgent arbitrageur = null;
        TradeStatisticCalculator arbStats = null;
        TradeStatisticCalculator allStats = null;
        if (la_present) {
            arbitrageur = new Arbitrageur(this, ALPHA, SHOW_ENTITIES_IN_TRACE);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            all.addMember(arbitrageur);
            arbStats = new TradeStatisticCalculator(this, "arbitrageur",
                    arb, DISCOUNT_RATE, getExperiment().getSimClock(), true, false);
            allStats = new TradeStatisticCalculator(this, "all",
                    all, DISCOUNT_RATE, getExperiment().getSimClock(), true, false);
            statsObjects.add(arbStats);
            statsObjects.add(allStats);
        }

        SimClock clock = this.getExperiment().getSimClock();
        VariableLimitFactory factory = new VariableLimitFactory(this, SIGMA_SHOCK, SIGMA_PV, k, MEAN_FUNDAMENTAL);
        for (int i = 0; i < numExchanges; i++) {
            Exchange exchange = new Exchange(this, "Exchange", sip, SHOW_ENTITIES_IN_TRACE);
            allExchanges.add(exchange);
            TradingAgentGroup group = new TradingAgentGroup();
            allExchangeGroups.add(group);

            TradeTimeSeries tradePrices = new TradeTimeSeries(this, "Exchange trade prices", group,
                    new TimeInstant(0.0), new TimeInstant(simLength), true, false);

            exchange.lastTradeSupplier.addObserver(tradePrices);
            exchange.lastTradeSupplier.addObserver(tradeStats);

            if (la_present) {
                exchange.lastTradeSupplier.addObserver(arbStats);
                exchange.lastTradeSupplier.addObserver(allStats);
                exchange.registerPriceObserver(arbitrageur);
            }

            for (int j = 0; j < 150; j++) {
                OrderRouter router = new BestPriceOrderRouter(clock, exchange);
                //Market 1
                TradingAgent agent = new ZIC(this, factory.create(), router, buyOrSell, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);
                exchange.registerPriceObserver(agent);
                sip.registerPriceObserver(agent); //TODO: Control this from the graph itself based on edges
                group.addMember(agent);
                tas.addMember(agent);
                all.addMember(agent);
                allTradingAgents.add(agent);
            }
        }

        WellmanGraph graph = new WellmanGraph(allTradingAgents, allExchanges, sip, getExperiment().getReferenceUnit());

        if (la_present) {
            graph.addVertex(arbitrageur);
            for (Exchange e: allExchanges)
                graph.addBidirectionalEdge(arbitrageur, e);
        }
        return graph;
    }

    @Override
    public ResultDto getResults() {
        ResultDto result = new ResultDto();
        result.delta = DELTA;
        for (TradeStatisticCalculator c : statsObjects) {
            Reporter r = c.createDefaultReporter();
            result.entries.add(r.getEntries());
        }
        return result;
    }
}
