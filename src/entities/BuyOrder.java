package entities;

import desmoj.core.simulator.*;

public class BuyOrder extends Order {

    public BuyOrder(Model owner, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(owner, name, showInTrace, source, dest);
    }

    @Override
    public void arrived() {
        dest.handleBuyOrder(this);
    }

    @Override
    public int getQueueingPriority() {
        return price;
    }
}
