package com.matt.marketsim.builders;

import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.models.MarketSimModel;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public interface NetworkBuilder {

    //Can either build the graph in code or build it from a file.
    SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model);
}
