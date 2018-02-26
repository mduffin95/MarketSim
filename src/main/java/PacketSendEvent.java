

import desmoj.core.simulator.*;

public class PacketSendEvent extends Event<Packet> {
    MarketSimModel model;
    public PacketSendEvent(Model owner) {
        super(owner, "PacketSendEvent", MarketSimModel.SHOW_EVENTS_IN_TRACE);
        model = (MarketSimModel) owner;

    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.scheduleArrival();
    }

}
