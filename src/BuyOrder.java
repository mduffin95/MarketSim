import desmoj.core.simulator.*;

public class BuyOrder extends Order {

    public BuyOrder(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public int getQueueingPriority() {
        return price;
    }
}
