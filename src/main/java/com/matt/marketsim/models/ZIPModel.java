package com.matt.marketsim.models;

import com.matt.marketsim.*;
import com.matt.marketsim.builders.Schedule;
import com.matt.marketsim.dtos.ResultDto;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.Arbitrageur;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIC;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.*;

public class ZIPModel extends TwoMarketModel {

    private Schedule schedule;
    private int buyAgents;
    private int sellAgents;

    public ZIPModel(ModelParameters params) {
        super(params);
        buyAgents = (int)params.getParameter("BUY_AGENTS_PER_EXCHANGE");
        sellAgents = (int)params.getParameter("SELL_AGENTS_PER_EXCHANGE");

        int minBuyLimit = (int)params.getParameter("MIN_BUY_LIMIT");
        int minSellLimit = (int)params.getParameter("MIN_SELL_LIMIT");
        int limitStep = (int)params.getParameter("LIMIT_STEP");
        schedule = new Schedule(minBuyLimit, limitStep, buyAgents, minSellLimit, limitStep, sellAgents);
    }

    @Override
    public String description() {
        return "A model using ZIP traders.";
    }

    @Override
    public void doInitialSchedules() {
        Collections.shuffle(initialAgents, generator);
        double cumulative = 0.0;
        for (TradingAgent a : initialAgents) {
            cumulative += agentArrivalTimeDist.sample();
            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", true, false);
            event.schedule(a, new TimeInstant(cumulative));
        }
    }


    //Create the network of entities
    @Override
    WellmanGraph createNetwork() {
        boolean la_present = (boolean)params.getParameter("LA_PRESENT");
        SecuritiesInformationProcessor sip = new SecuritiesInformationProcessor(this, "Securities Information Processor",
                SHOW_ENTITIES_IN_TRACE, new TimeSpan((double)params.getParameter("DELTA")));
        Set<Exchange> allExchanges = new HashSet<>();
        Set<TradingAgent> allTradingAgents = new HashSet<>();
        List<TradingAgentGroup> allExchangeGroups = new ArrayList<>();

        TradingAgentGroup tas = new TradingAgentGroup();
        TradingAgentGroup all = new TradingAgentGroup();

        TradeStatisticCalculator tradeStats = new TradeStatisticCalculator(this, "trading_agents", tas,
                (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
        statsObjects.add(tradeStats);

        TradingAgent arbitrageur = null;
        TradeStatisticCalculator arbStats = null;
        TradeStatisticCalculator allStats = null;
        if (la_present) {
            arbitrageur = new Arbitrageur(this, (double)params.getParameter("ALPHA"), SHOW_ENTITIES_IN_TRACE);
            TradingAgentGroup arb = new TradingAgentGroup();
            arb.addMember(arbitrageur);
            all.addMember(arbitrageur);
            arbStats = new TradeStatisticCalculator(this, "arbitrageur",
                    arb, (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            allStats = new TradeStatisticCalculator(this, "all",
                    all, (double)params.getParameter("DISCOUNT_RATE"), getExperiment().getSimClock(), true, false);
            statsObjects.add(arbStats);
            statsObjects.add(allStats);
        }

        SimClock clock = this.getExperiment().getSimClock();
        for (int i = 0; i < (int)params.getParameter("NUM_EXCHANGES"); i++) {
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

            for (int j = 0; j < buyAgents; j++) {
                OrderRouter router = new BestPriceOrderRouter(clock, exchange);
                //Market 1
                TradingAgent agent = new ZIP(this, schedule.getBuySchedule()[j], router, Direction.BUY, generator, SHOW_ENTITIES_IN_TRACE);
                exchange.registerPriceObserver(agent);
                sip.registerPriceObserver(agent); //TODO: Control this from the graph itself based on edges
                group.addMember(agent);
                tas.addMember(agent);
                all.addMember(agent);
                allTradingAgents.add(agent);
            }
            for (int j = 0; j < sellAgents; j++) {
                OrderRouter router = new BestPriceOrderRouter(clock, exchange);
                //Market 1
                TradingAgent agent = new ZIP(this, schedule.getSellSchedule()[j], router, Direction.SELL, generator, SHOW_ENTITIES_IN_TRACE);
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
}
