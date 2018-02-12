import desmoj.core.simulator.*;

public abstract class Order extends Entity {
    protected int price;
    protected TradingAgent ta;

    public Order(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

}
