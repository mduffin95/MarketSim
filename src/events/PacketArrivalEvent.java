package events;

import desmoj.core.simulator.*;
import entities.*;

public class PacketArrivalEvent extends Event<Packet> {

    public PacketArrivalEvent(Model model) {
        super(model, "PacketArrivalEvent", true);
    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.arrived();
    }
}
