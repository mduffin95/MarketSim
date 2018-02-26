package com.matt.marketsim.events;

import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;

public class PacketSendEvent extends Event<Packet> {
    MarketSimModel model;
    public PacketSendEvent(Model owner) {
        super(owner, "com.matt.marketsim.events.PacketSendEvent", MarketSimModel.SHOW_EVENTS_IN_TRACE);
        model = (MarketSimModel) owner;

    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.scheduleArrival();
    }

}
