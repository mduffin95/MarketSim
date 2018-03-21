package com.matt.marketsim;

import com.matt.marketsim.entities.NetworkEntity;

public interface MarketGraph {
    boolean addEdge(NetworkEntity a, NetworkEntity b, double weight);
    boolean addEdge(NetworkEntity a, NetworkEntity b);
    void addVertex(NetworkEntity entity);
    TimedEdge getEdge(NetworkEntity a, NetworkEntity b);
    double getEdgeWeight(NetworkEntity a, NetworkEntity b);
    double getEdgeWeight(TimedEdge edge);

}
