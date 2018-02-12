package entities;

import desmoj.core.simulator.*;

public abstract class NetworkEntity extends Entity {

    public NetworkEntity(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    public abstract void handleSellOrder(SellOrder sellOrder);
    public abstract void handleBuyOrder(BuyOrder buyOrder);
    public abstract void handlePriceUpdate(PriceUpdate priceUpdate);
}
