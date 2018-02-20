import desmoj.core.simulator.*;

public class PacketArrivalEvent extends Event<Packet> {

    public PacketArrivalEvent(Model model) {
        super(model, "PacketArrivalEvent", MarketSimModel.SHOW_EVENTS_IN_TRACE);
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.arrived();
    }
}
