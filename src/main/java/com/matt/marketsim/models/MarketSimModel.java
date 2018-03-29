package com.matt.marketsim.models;

import com.matt.marketsim.WellmanGraph;
import com.matt.marketsim.dtos.ResultDto;
import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.dist.DistributionManager;
import desmoj.core.simulator.*;
import com.matt.marketsim.entities.NetworkEntity;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public abstract class MarketSimModel extends Model {

    int simLength;
    public DistributionManager distributionManager;
    Random generator;
    WellmanGraph network;

    /*
     * Model entities
     */
    ArrayList<TradingAgent> initialAgents;

    public MarketSimModel(Model model, String name, boolean showInReport, boolean showInTrace, int simLength) {
        super(model, name, showInReport, showInTrace);
        initialAgents = new ArrayList<>();
        this.simLength = simLength;
    }

    //Agent arrival time.
    public abstract TimeSpan getAgentArrivalTime();

    protected abstract WellmanGraph getNetwork();

    public abstract boolean showPacketSendInTrace();
    public abstract boolean showPacketArrivalInTrace();

    abstract WellmanGraph createNetwork();


    @Override
    public void init() {
        long seed = generator.nextLong();
        distributionManager = new DistributionManager("Distribution Manager", seed);
//        getExperiment().setReferenceUnit(TimeUnit.MILLISECONDS);
    }


    public void registerForInitialSchedule(TradingAgent agent) {
        initialAgents.add(agent);
    }

    public TimeSpan getLatency(NetworkEntity a, NetworkEntity b) {
        return new TimeSpan(getNetwork().getEdgeWeight(a, b));
    }

    public void setSeed(long s) {
        generator.setSeed(s);
    }

    public abstract ResultDto getResults();
}
