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
import desmoj.core.simulator.*;

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
    private ParameterManager params;


    public TwoMarketModel(ParameterManager params) {
        super(null, "TwoMarketModel", true, true, (int)params.getParameterValue("SIM_LENGTH"));
        generator = new Random();
        this.params = params;

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
                (1.0 / (double)params.getParameterValue("LAMBDA")), true, false);
        agentArrivalTimeDist.setNonNegative(true);
        offsetRangeDist = new ContDistUniform(this, "OffsetRangeUniformStream",
                0, (double)params.getParameterValue("OFFSET_RANGE"), true, false);

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
        boolean la_present = false;
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan((double)params.getParameterValue("DELTA")));
        Set<Exchange> allExchanges = new HashSet<>();
        Set<TradingAgent> allTradingAgents = new HashSet<>();
        List<TradingAgentGroup> allExchangeGroups = new ArrayList<>();

        TradingAgentGroup tas = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(this, "trading_agents", tas,
                (double)params.getParameterValue("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
        statsObjects.add(tradeStats);

        TradingAgent arbitrageur = null;
        TradeStatisticCalculator arbStats = null;
        TradeStatisticCalculator allStats = null;
        if (la_present) {
            arbitrageur = new Arbitrageur(this, (double)params.getParameterValue("ALPHA"), SHOW_ENTITIES_IN_TRACE);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            all.addMember(arbitrageur);
            arbStats = new TradeStatisticCalculator(this, "arbitrageur",
                    arb, (double)params.getParameterValue("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            allStats = new TradeStatisticCalculator(this, "all",
                    all, (double)params.getParameterValue("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            statsObjects.add(arbStats);
            statsObjects.add(allStats);
        }

        SimClock clock = this.getExperiment().getSimClock();
        VariableLimitFactory factory = new VariableLimitFactory(this,
                (double)params.getParameterValue("SIGMA_SHOCK"),
                (double)params.getParameterValue("SIGMA_PV"),
                (double)params.getParameterValue("K"),
                (double)params.getParameterValue("MEAN_FUNDAMENTAL"));
        for (int i = 0; i < (int)params.getParameterValue("NUM_EXCHANGES"); i++) {
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

            for (int j = 0; j < (int)params.getParameterValue("AGENTS_PER_EXCHANGE"); j++) {
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
        result.delta = (double)params.getParameterValue("DELTA");
        for (TradeStatisticCalculator c : statsObjects) {
            Reporter r = c.createDefaultReporter();
            result.entries.add(r.getEntries());
        }
        return result;
    }
}
