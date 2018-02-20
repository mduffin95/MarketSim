import desmoj.core.simulator.*;

public class PacketSendEvent extends Event<Packet> {
    public PacketSendEvent(Model owner) {
        super(owner, "PacketSendEvent", MarketSimModel.SHOW_EVENTS_IN_TRACE);

    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.scheduleArrival();
    }
}
