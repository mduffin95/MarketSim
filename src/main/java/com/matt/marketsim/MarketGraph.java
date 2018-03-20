package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/*
 * A wrapper around the JgraphT graph to make it a bit easier to use.
 */
public class MarketGraph {
    private SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> graph;

    public MarketGraph() {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public void addEdge(NetworkEntity a, NetworkEntity b, double weight) {
        graph.setEdgeWeight(graph.addEdge(a, b), weight);
    }

    public void addEdge(NetworkEntity a, NetworkEntity b) {
        this.addEdge(a, b, 0);
    }

    public void addVertex(NetworkEntity entity) {
        graph.addVertex(entity);
    }

    public DefaultWeightedEdge getEdge(NetworkEntity a, NetworkEntity b) {
        return graph.getEdge(a, b);
    }

    public double getEdgeWeight(NetworkEntity a, NetworkEntity b) {
        return this.getEdgeWeight(this.getEdge(a, b));
    }
    public double getEdgeWeight(DefaultWeightedEdge edge) {
        return graph.getEdgeWeight(edge);
    }
}
