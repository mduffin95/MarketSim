import desmoj.core.simulator.*;

public class PacketArrivalEvent extends Event<Packet> {

    public PacketArrivalEvent(Model model) {
        super(model, "PacketArrivalEvent", true);
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.arrived();
        sendTraceNote("Packet arrived.");
    }
}
