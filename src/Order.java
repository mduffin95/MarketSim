import desmoj.core.simulator.*;

public abstract class Order extends Packet {
    protected int price;
    protected TradingAgent agent;

    public Order(Model owner, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(owner, name, showInTrace, source, dest);
    }

    public int getPrice() {
        return price;
    }
}