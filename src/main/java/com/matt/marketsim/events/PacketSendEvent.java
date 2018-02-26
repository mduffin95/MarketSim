package com.matt.marketsim.events;

import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

//TODO: Could do away with this class
public class PacketSendEvent extends Event<Packet> {
    public PacketSendEvent(Model owner) {
        super(owner, "PacketSendEvent", MarketSimModel.PACKET_SEND_IN_TRACE);
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.scheduleArrival();
    }

}
