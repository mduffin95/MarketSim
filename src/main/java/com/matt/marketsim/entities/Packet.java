package com.matt.marketsim.entities;

import com.matt.marketsim.MessageType;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;
import com.matt.marketsim.events.PacketArrivalEvent;

public class Packet extends Entity implements IPacket {
    private NetworkEntity source;
    private NetworkEntity dest;

    private MessageType type;
    private Object payload;

    private MarketSimModel marketSimModel;
    private boolean showInTrace;

    public Packet(Model model, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest,
                  MessageType type, Object payload) {
        super(model, name, showInTrace);
        this.source = source;
        this.dest = dest;
        this.type = type;
        this.payload = payload;
        this.showInTrace = showInTrace;

        marketSimModel = (MarketSimModel) model;
    }

    public Packet(Model model, NetworkEntity source, NetworkEntity dest, MessageType type, Object payload) {
        this(model, "Packet", false, source, dest, type, payload);
    }

    @Override
    public void scheduleArrival() {
        TimeSpan latency = marketSimModel.getLatency(source, dest);

        PacketArrivalEvent packetArrivalEvent = new PacketArrivalEvent(marketSimModel);

        packetArrivalEvent.schedule(this, latency);
    }

    public NetworkEntity getDest() {
        return dest;
    }

    public NetworkEntity getSource() {
        return source;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void arrived() {
        dest.handlePacket(this);
    }
}
