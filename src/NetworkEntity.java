import desmoj.core.simulator.*;

public abstract class NetworkEntity extends Entity {

    public NetworkEntity(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    public abstract void handlePacket(Packet packet);
}
