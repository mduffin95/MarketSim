import desmoj.core.simulator.TimeSpan;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface NetworkBuilder {

    //Can either build the graph in code or build it from a file.
    SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model);
}
