package com.matt.marketsim.events;

import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

//TODO: Could do away with this class
public class PacketSendEvent extends Event<Packet> {
    public PacketSendEvent(MarketSimModel model) {
        super(model, "PacketSendEvent", model.showPacketSendInTrace());
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.scheduleArrival();
    }
}
