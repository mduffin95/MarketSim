package entities;

import desmoj.core.simulator.*;
import entities.Order;
import models.MarketSimModel;

public class SellOrder extends Order {
    MarketSimModel marketSimModel;

    public SellOrder(Model owner, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(owner, name, showInTrace, source, dest);
        marketSimModel = (MarketSimModel) owner;
    }

    @Override
    public void arrived() {
        dest.handleSellOrder(this);
    }

    @Override
    public int getQueueingPriority() {
        return marketSimModel.MAX_PRICE - price;
    }
}
