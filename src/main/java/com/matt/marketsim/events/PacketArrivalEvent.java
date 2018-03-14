package com.matt.marketsim.events;

import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

public class PacketArrivalEvent extends Event<Packet> {

    public PacketArrivalEvent(MarketSimModel model) {
        super(model, "PacketArrivalEvent",  model.showPacketArrivalInTrace());
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.arrived();
    }
}
