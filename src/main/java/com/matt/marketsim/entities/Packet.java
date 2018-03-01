package com.matt.marketsim.entities;

import com.matt.marketsim.MessageType;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;
import com.matt.marketsim.events.PacketArrivalEvent;

public class Packet extends Entity {
    private NetworkEntity source;
    private NetworkEntity dest;

    private MessageType type;
    private Object payload;

    private MarketSimModel marketSimModel;

    public Packet(Model model, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest,
                  MessageType type, Object payload) {
        super(model, name, showInTrace);
        this.source = source;
        this.dest = dest;
        this.type = type;
        this.payload = payload;

        marketSimModel = (MarketSimModel) model;
    }

    public Packet(Model model, NetworkEntity source, NetworkEntity dest, MessageType type, Object payload) {
        this(model, "Packet", false, source, dest, type, payload);
    }

    public void scheduleArrival() {
        TimeSpan latency = marketSimModel.getLatency(source, dest);

        PacketArrivalEvent packetArrivalEvent = new PacketArrivalEvent(marketSimModel);

        packetArrivalEvent.schedule(this, latency);
    }

    public com.matt.marketsim.entities.NetworkEntity getDest() {
        return dest;
    }

    public com.matt.marketsim.entities.NetworkEntity getSource() {
        return source;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public void arrived() {
        dest.handlePacket(this);
    }
}
