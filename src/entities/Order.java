package entities;

import desmoj.core.simulator.*;
import events.PacketArrivalEvent;

public abstract class Order extends Packet {
    protected int price;
    protected TradingAgent ta;

    public Order(Model owner, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(owner, name, showInTrace, source, dest);
    }

    public int getPrice() {
        return price;
    }
}