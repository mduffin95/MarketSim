package entities;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import desmoj.core.simulator.*;
import events.PacketArrivalEvent;
import models.MarketSimModel;

import java.util.concurrent.TimeUnit;

public abstract class Packet extends Entity {
    protected NetworkEntity source;
    protected NetworkEntity dest;

    private MarketSimModel marketSimModel;

    public Packet(Model model, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(model, name, showInTrace);
        this.source = source;
        this.dest = dest;

        marketSimModel = (MarketSimModel) model;
    }

    public Packet(Model model, NetworkEntity source, NetworkEntity dest) {
        this(model, "Packet", false, source, dest);
    }

    public void send() {
        long latency = marketSimModel.getLatency(source, dest);

        PacketArrivalEvent packetArrivalEvent = new PacketArrivalEvent(marketSimModel);

        packetArrivalEvent.schedule(this, new TimeSpan(latency, TimeUnit.SECONDS));
    }

    public NetworkEntity getDest() {
        return dest;
    }

    public NetworkEntity getSource() {
        return source;
    }

    public abstract void arrived();
}
