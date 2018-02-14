import desmoj.core.simulator.*;

public class PacketSendEvent extends Event<Packet> {
    public PacketSendEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

    }

    @Override
    public void eventRoutine(Packet packet) {
        packet.send();
    }
}
