package com.matt.marketsim.events;

import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

public class PacketArrivalEvent extends Event<Packet> {

    public PacketArrivalEvent(Model model) {
        super(model, "PacketArrivalEvent",  MarketSimModel.PACKET_ARRIVAL_IN_TRACE);
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.arrived();
    }
}
