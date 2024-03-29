package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.Schedule;
import com.matt.marketsim.entities.CDA;
import com.matt.marketsim.entities.Call;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.*;
import com.matt.marketsim.statistics.RoutingStatistics;
import com.matt.marketsim.statistics.TradeStatistics;
import com.matt.marketsim.statistics.TradeTimeSeries;
import com.matt.marketsim.statistics.TradingAgentGroup;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.*;

public class ZIPModel extends TwoMarketModel {

    private Schedule schedule;
    private int scheduleLength;
    private int perPrice;

    public ZIPModel(ModelParameters params) {
        super(params);
        scheduleLength = (int)params.getParameter("SCHEDULE_LENGTH");
        perPrice = (int)params.getParameter("AGENTS_PER_PRICE");

        int minBuyLimit = (int)params.getParameter("MIN_BUY_LIMIT");
        int minSellLimit = (int)params.getParameter("MIN_SELL_LIMIT");
        int limitStep = (int)params.getParameter("LIMIT_STEP");
        schedule = new Schedule(minBuyLimit, limitStep, scheduleLength, minSellLimit, limitStep, scheduleLength);
    }

    @Override
    public String description() {
        return "A model using ZIP traders.";
    }

//    @Override
//    public void doInitialSchedules() {
//        Collections.shuffle(initialAgents, generator);
//        double cumulative = 0.0;
//        for (TradingAgent a : initialAgents) {
//            cumulative += agentArrivalTimeDist.sample();
//            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", true, false);
//            event.schedule(a, new TimeInstant(cumulative));
//        }
//    }


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

        double discount_rate = (double)params.getParameter("DISCOUNT_RATE");
        int equilibrium = (int)params.getParameter("EQUILIBRIUM");
        TradeStatistics tradeStats = new TradeStatistics(this, "trading_agents", tas,
                discount_rate, getExperiment().getSimClock(), true, false, equilibrium);
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
                    arb, discount_rate, getExperiment().getSimClock(), true, false, equilibrium);
            allStats = new TradeStatistics(this, "all",
                    all, discount_rate, getExperiment().getSimClock(), true, false, equilibrium);
            tradeStatsObjects.add(arbStats);
            tradeStatsObjects.add(allStats);
        }

        SimClock clock = this.getExperiment().getSimClock();
        int numExchanges = (int)params.getParameter("NUM_EXCHANGES");
        for (int i = 0; i < numExchanges; i++) {
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

            for (int j = 0; j < scheduleLength; j++) {
                for (int k=0; k < perPrice; k++) {
                    //BUY AGENTS
                    BestPriceOrderRouter router = new BestPriceOrderRouter(clock, exchange, allExchanges);
                    router.routingSupplier.addObserver(routeStats);
                    //Market 1
                    TradingAgent agent;
                    if (params.getParameter("TRADING_AGENT").equals("ZIC")) {
                        agent = new ZIC(this, new FixedLimit(schedule.getBuySchedule()[j]), router, Direction.BUY, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);
                    } else {
                        agent = new ZIP(this, schedule.getBuySchedule()[j], router, Direction.BUY, generator, SHOW_ENTITIES_IN_TRACE);
                    }
                    exchange.registerPriceObserver(agent);
                    sip.registerPriceObserver(agent); //TODO: Control this from the graph itself based on edges
                    group.addMember(agent);
                    tas.addMember(agent);
                    all.addMember(agent);
                    allTradingAgents.add(agent);

                    //SELL AGENTS
                    router = new BestPriceOrderRouter(clock, exchange, allExchanges);
                    router.routingSupplier.addObserver(routeStats);
                    //Market 1
                    if (params.getParameter("TRADING_AGENT").equals("ZIC")) {
                        agent = new ZIC(this, new FixedLimit(schedule.getSellSchedule()[j]), router, Direction.SELL, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);
                    } else {
                        agent = new ZIP(this, schedule.getSellSchedule()[j], router, Direction.SELL, generator, SHOW_ENTITIES_IN_TRACE);
                    }

//                TradingAgent agent = new ZIP(this, schedule.getSellSchedule()[j], router, Direction.SELL, generator, SHOW_ENTITIES_IN_TRACE);
//                TradingAgent agent = new ZIC(this, new FixedLimit(schedule.getSellSchedule()[j]), router, Direction.SELL, offsetRangeDist, SHOW_ENTITIES_IN_TRACE);
                    exchange.registerPriceObserver(agent);
                    sip.registerPriceObserver(agent); //TODO: Control this from the graph itself based on edges
                    group.addMember(agent);
                    tas.addMember(agent);
                    all.addMember(agent);
                    allTradingAgents.add(agent);
                }
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
}
