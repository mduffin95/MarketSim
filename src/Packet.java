import desmoj.core.simulator.*;

public class Packet extends Entity {
    private NetworkEntity source;
    private NetworkEntity dest;

    private Payload payload;

    private MarketSimModel marketSimModel;

    public Packet(Model model, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest,
                  Payload payload) {
        super(model, name, showInTrace);
        this.source = source;
        this.dest = dest;
        this.payload = payload;

        marketSimModel = (MarketSimModel) model;
    }

    public Packet(Model model, NetworkEntity source, NetworkEntity dest, Payload payload) {
        this(model, "Packet", false, source, dest, payload);
    }

    public void scheduleArrival() {
        TimeSpan latency = marketSimModel.getLatency(source, dest);

        PacketArrivalEvent packetArrivalEvent = new PacketArrivalEvent(marketSimModel);

        packetArrivalEvent.schedule(this, latency);
    }

    public NetworkEntity getDest() {
        return dest;
    }

    public NetworkEntity getSource() {
        return source;
    }

    public Payload getPayload() {
        return payload;
    }

    public void arrived() {
        dest.handlePacket(this);
    }
}
