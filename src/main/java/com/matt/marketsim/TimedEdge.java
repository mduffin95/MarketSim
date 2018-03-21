package com.matt.marketsim;

import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.concurrent.TimeUnit;

public class TimedEdge<V> extends DefaultWeightedEdge {
    TimeUnit unit;

    public TimedEdge(TimeUnit unit) {
        this.unit = unit;
    }

    public TimeSpan getTimedWeight() {
        return new TimeSpan(this.getWeight(), unit);
    }
}
