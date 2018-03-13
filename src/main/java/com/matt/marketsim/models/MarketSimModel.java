package com.matt.marketsim.models;

import com.matt.marketsim.entities.agents.TradingAgent;
import desmoj.core.simulator.*;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.events.TradingAgentDecisionEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public abstract class MarketSimModel extends Model {

    protected TimeUnit timeUnit;
    protected int simLength;

    /*
     * Model entities
     */
    private ArrayList<TradingAgent> initialAgents;

    public MarketSimModel(Model model, String name, boolean showInReport, boolean showInTrace, TimeUnit timeUnit, int simLength) {
        super(model, name, showInReport, showInTrace);
        this.timeUnit = timeUnit;
        initialAgents = new ArrayList<>();
        this.simLength = simLength;
    }

    //Agent arrival time.
    protected abstract TimeSpan getAgentArrivalTime();

    protected abstract SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> getNetwork();

    public abstract void setSeed(long seed);

    @Override
    public void doInitialSchedules() {
        for (TradingAgent a: initialAgents) {
            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", true);
            event.schedule(a, getAgentArrivalTime());
        }
    }

    public void registerForInitialSchedule(TradingAgent agent) {
        initialAgents.add(agent);
    }

    public TimeSpan getLatency(NetworkEntity a, NetworkEntity b) {
        //TODO: Implement adjacency matrix
        DefaultWeightedEdge edge = getNetwork().getEdge(a, b);
        return new TimeSpan(getNetwork().getEdgeWeight(edge), timeUnit);
    }


}
