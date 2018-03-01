package com.matt.marketsim.builders;

import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.statistic.StatisticObject;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.Observer;

public interface NetworkBuilder {

    //Can either build the graph in code or build it from a file. Also has a list of observers that need to be attached
    //to exchanges that are created.
    SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model);

    int getEquilibriumPrice();
    int getTheoreticalUtility();
}
