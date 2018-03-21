package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.TradingAgent;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * A wrapper around the JgraphT graph to make it a bit easier to use.
 */
public class WellmanGraph implements MarketGraph {
    private SimpleDirectedWeightedGraph<NetworkEntity, TimedEdge> graph;
    private TimeUnit unit;

    public WellmanGraph(Set<TradingAgent> tas, Set<Exchange> exchanges, SecuritiesInformationProcessor sip, TimeUnit unit) {
        graph = new SimpleDirectedWeightedGraph<>(TimedEdge.class);
        this.unit = unit;

        addVertex(sip);

        //Connect each exchange to the SIP
        for (Exchange e: exchanges) {
            addVertex(e);
            addEdge(e, sip);
        }

        //Connect the SIP to every trading agent
        //Connect each trading agent to every exchange (bidirectional)
        for (TradingAgent ta: tas) {
            addVertex(ta);
            addEdge(sip, ta);
            for (Exchange e: exchanges) {
                addEdge(ta, e);
                addEdge(e, ta);
            }
        }
    }

    public boolean addEdge(NetworkEntity a, NetworkEntity b, double weight) {
        TimedEdge edge = new TimedEdge(unit);
        if (graph.addEdge(a, b, edge)) {
            graph.setEdgeWeight(edge, weight);
            return true;
        }
        return false;
    }

    public boolean addEdge(NetworkEntity a, NetworkEntity b) {
        return this.addEdge(a, b, 0);
    }

    public boolean addBidirectionalEdge(NetworkEntity a, NetworkEntity b) {
        return this.addEdge(a, b) && this.addEdge(b, a);
    }

    public void addVertex(NetworkEntity entity) {
        graph.addVertex(entity);
    }

    public TimedEdge getEdge(NetworkEntity a, NetworkEntity b) {
        return graph.getEdge(a, b);
    }

    public double getEdgeWeight(NetworkEntity a, NetworkEntity b) {
        return this.getEdgeWeight(this.getEdge(a, b));
    }
    public double getEdgeWeight(TimedEdge edge) {
        return graph.getEdgeWeight(edge);
    }
}
