package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.dtos.ResultDto;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import com.matt.marketsim.statistics.RoutingStatistics;
import com.matt.marketsim.statistics.TradeStatistics;
import com.matt.marketsim.statistics.TradeTimeSeries;
import com.matt.marketsim.statistics.TradingAgentGroup;
import desmoj.core.dist.*;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.*;

import java.util.*;

public class TwoMarketModel extends MarketSimModel {

    /*
     * Distributions and number generators
     */
    ContDistExponential agentArrivalTimeDist;
    ContDistUniform offsetRangeDist;
//    protected DiscreteDistUniform priceDist;
    BoolDistBernoulli buyOrSell;
    //    private BoolDistBernoulli buyOrSell;

    /*
     * Model parameters
     */

    boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = true;
    private static boolean PACKET_SEND_IN_TRACE = false;
    private static boolean PACKET_ARRIVAL_IN_TRACE = false;

    /*
     * Statistics
     */
    List<TradeStatistics> tradeStatsObjects;
    List<RoutingStatistics> routeStatsObjects;


    /*
     * Wellman's parameters
     */
    protected ModelParameters params;

    /*
     * Other
     */
    Set<Exchange> allExchanges;
    SecuritiesInformationProcessor sip;
    public VariableLimitFactory factory;

    public TwoMarketModel(ModelParameters params) {
        super(null, "TwoMarketModel", true, true, (int)params.getParameter("SIM_LENGTH"));
        generator = new Random();
        this.params = params;

        tradeStatsObjects = new ArrayList<>();
        routeStatsObjects = new ArrayList<>();
        allExchanges = new HashSet<>();
    }

    @Override
    public String description() {
        return "A two market model from the Wah and Wellman paper.";
    }

    @Override
    public void doInitialSchedules() {
        List l = (List) initialAgents;
        Collections.shuffle(l, generator);
        TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", true, initialAgents);
        event.schedule(initialAgents.remove(), new TimeInstant(agentArrivalTimeDist.sample()));
    }

    @Override
    public void init() {
        super.init();

        /*
         * Distributions and number generators
         */

        agentArrivalTimeDist = new ContDistExponential(this, "AgentArrivalTimeStream",
                (1.0 / (double)params.getParameter("LAMBDA")), true, false);
        agentArrivalTimeDist.setNonNegative(true);
        offsetRangeDist = new ContDistUniform(this, "OffsetRangeUniformStream",
                0, (double)params.getParameter("OFFSET_RANGE"), true, false);

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
        boolean la_present = (boolean)params.getParameter("LA_PRESENT");
        sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan((double)params.getParameter("DELTA")));
        Set<TradingAgent> allTradingAgents = new HashSet<>();
        List<TradingAgentGroup> allExchangeGroups = new ArrayList<>();

        TradingAgentGroup tas = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();

        TradeStatistics tradeStats = new TradeStatistics(this, "trading_agents", tas,
                (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
        tradeStatsObjects.add(tradeStats);

        RoutingStatistics routeStats = new RoutingStatistics(this, "route_stats", tas, true, false);
        routeStatsObjects.add(routeStats);

        TradingAgent arbitrageur = null;
        TradeStatistics arbStats = null;
        TradeStatistics allStats = null;
        if (la_present) {
            arbitrageur = new Arbitrageur(this, (double)params.getParameter("ALPHA"), SHOW_ENTITIES_IN_TRACE);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            all.addMember(arbitrageur);
            arbStats = new TradeStatistics(this, "arbitrageur",
                    arb, (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            allStats = new TradeStatistics(this, "all",
                    all, (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            tradeStatsObjects.add(arbStats);
            tradeStatsObjects.add(allStats);
        }

        SimClock clock = this.getExperiment().getSimClock();
        factory = new VariableLimitFactory(this,
                (double)params.getParameter("SIGMA_SHOCK"),
                (double)params.getParameter("SIGMA_PV"),
                (double)params.getParameter("K"),
                (double)params.getParameter("MEAN_FUNDAMENTAL"));
        for (int i = 0; i < (int)params.getParameter("NUM_EXCHANGES"); i++) {
            Exchange exchange;
            if (params.getParameter("EXCHANGE_TYPE").equals("CDA")) {
                exchange = new CDA(this, "Exchange", sip, SHOW_ENTITIES_IN_TRACE);
            } else {
                exchange = new Call(this, "PeriodicCallMarket", sip, SHOW_ENTITIES_IN_TRACE,
                        new TimeSpan((double)params.getParameter("DELTA")));
            }

            allExchanges.add(exchange);
            TradingAgentGroup group = new TradingAgentGroup();
            allExchangeGroups.add(group);

            TradeTimeSeries tradePrices = new TradeTimeSeries(this, "Exchange trade prices", group,
                    new TimeInstant(0.0), new TimeInstant(simLength), true, false);

            exchange.registerLastTradeObserver(tradePrices);
            exchange.registerLastTradeObserver(tradeStats);

            if (la_present) {
                exchange.registerLastTradeObserver(arbStats);
                exchange.registerLastTradeObserver(allStats);
                exchange.registerPriceObserver(arbitrageur);
            }

            for (int j = 0; j < (int)params.getParameter("AGENTS_PER_EXCHANGE"); j++) {
                BestPriceOrderRouter router = new BestPriceOrderRouter(clock, exchange, allExchanges);
                router.routingSupplier.addObserver(routeStats);
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
            for (Exchange e: allExchanges) {
                graph.addBidirectionalEdge(arbitrageur, e); //Bit of a hack
            }

        }
        return graph;
    }

    @Override
    public ResultDto getResults() {
        ResultDto result = new ResultDto();
        result.params = params;
        for (TradeStatistics stats : tradeStatsObjects) {
            Reporter r = stats.createDefaultReporter();
            result.entries.add(r.getEntries());
        }
        for (RoutingStatistics stats : routeStatsObjects) {
            Reporter r = stats.createDefaultReporter();
            result.entries.add(r.getEntries());
        }
        for (Exchange e: allExchanges) {
            Reporter r = e.stats.createDefaultReporter();
            result.entries.add(r.getEntries());
        }
        Reporter r = sip.stats.createDefaultReporter();
        result.entries.add(r.getEntries());
        return result;
    }
}
