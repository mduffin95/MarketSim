package com.matt.marketsim.models;

import com.matt.marketsim.Trade;
import com.matt.marketsim.TradeStatisticCalculator;
import com.matt.marketsim.TradeTimeSeries;
import com.matt.marketsim.builders.DifferentDelay;
import com.matt.marketsim.builders.NetworkBuilder;
import com.matt.marketsim.builders.Wellman;
import com.matt.marketsim.builders.ZIPExperiment;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.StatisticObject;
import desmoj.core.statistic.TimeSeries;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MarketSimModel extends Model {

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

    /*
     * Network Builder
     */
    private NetworkBuilder builder;


    /*
     * Model parameters
     */

    public static int MIN_PRICE = 1;
    public static int MAX_PRICE = 200;
    public static int OFFSET_RANGE = 40;
    public static int MEAN_TIME_BETWEEN_TRADES = 10;
    public static int SIM_LENGTH = 500;
    public static boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = true;
    public static boolean PACKET_SEND_IN_TRACE = true;
    public static boolean PACKET_ARRIVAL_IN_TRACE = true;
    public static TimeUnit timeUnit = TimeUnit.SECONDS;

    /*
     * Model entities
     */
    private ArrayList<TradingAgent> agents;
    SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> network;

    /*
     * Methods
     */


    public MarketSimModel(NetworkBuilder builder) {
        super(null, "ExchangeModel", true, true);
        generator = new Random();
        this.builder = builder;
        setSeed(1);

    }

    @Override
    public String description() {
        return "Market simulator model.";
    }

    @Override
    public void doInitialSchedules() {
        for (TradingAgent a: agents) {
            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);
            event.schedule(a, getAgentArrivalTime());
        }
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
        agentArrivalTimeUniform = new ContDistUniform(this, "AgentArrivalTimeUniformStream",
                0, 100, true, false);
        offsetRange = new ContDistUniform(this, "OffsetRangeUniformStream",
                0, OFFSET_RANGE, true, false);
        priceDist = new DiscreteDistUniform(this, "LimitPriceStream", MIN_PRICE, MAX_PRICE,
                true, false);
//        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        distributionManager.register(agentArrivalTime);
        distributionManager.register(agentArrivalTimeUniform);
        distributionManager.register(priceDist);
//        distributionManager.register(buyOrSell);

        /*
         * Entities
         */
        agents = new ArrayList<>();


        //Also calculates theoretical equilibrium price
        network = builder.createNetwork(this);

    }


    public TimeSpan getAgentArrivalTime() {
//        return agentArrivalTime.sample();
        return new TimeSpan(agentArrivalTime.sample(), MarketSimModel.timeUnit);
    }

    public int getRandomPrice() {
        return priceDist.sample().intValue();
    }
//
//    public boolean getBuyOrSell() {
//        return buyOrSell.sample();
//    }

    public TimeSpan getLatency(NetworkEntity a, NetworkEntity b) {
        //TODO: Implement adjacency matrix
        DefaultWeightedEdge edge = network.getEdge(a, b);
        return new TimeSpan(network.getEdgeWeight(edge), MarketSimModel.timeUnit);
    }

    public void setSeed(long s) {
        generator.setSeed(s);
    }

    public void registerForInitialSchedule(TradingAgent agent) {
        agents.add(agent);
    }

    public Set<NetworkEntity> getConnectedVertices(NetworkEntity entity) {
        Set<DefaultWeightedEdge> edges = network.edgesOf(entity);
        Set<NetworkEntity> connected = new HashSet<>();
        for (DefaultWeightedEdge e: edges) {
            NetworkEntity source = network.getEdgeSource(e);
            NetworkEntity dest = network.getEdgeTarget(e);
            if (source != entity) {
                connected.add(source);
            } else {
                connected.add(dest);
            }
        }
        return connected;
    }

    /**
     * runs the model
     */
    public static void main(String[] args) {

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
//        NetworkBuilder builder = new ZIPExperiment(50, 0, 200);
        NetworkBuilder builder = new Wellman();
        MarketSimModel model = new MarketSimModel(builder);
//        model.setSeed(1);
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(MarketSimModel.SIM_LENGTH, MarketSimModel.timeUnit);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }
}
