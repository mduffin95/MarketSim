

import desmoj.core.simulator.*;

public abstract class NetworkEntity extends Entity {
    MarketSimModel marketSimModel;

    public NetworkEntity(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel) model;
    }

    //Used to handle incoming packets
    public abstract void handlePacket(Packet packet);

    //Send a payload to this main.java.NetworkEntity from the source main.java.NetworkEntity
    public void send(NetworkEntity source, MessageType type, Object payload) {
        send(source, type, payload, new TimeSpan(0));
    }

    public void send(NetworkEntity source, MessageType type, Object payload, TimeSpan delay) {
        String name = "PacketFrom" + source.getName() + "To" + this.getName();
        Packet packet = new Packet(marketSimModel, name, true, source, this, type, payload);

        PacketSendEvent sendEvent = new PacketSendEvent(marketSimModel);
        sendEvent.schedule(packet, delay);
    }
}
