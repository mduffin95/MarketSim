import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;

public class SecuritiesInformationProcessor extends NetworkEntity implements PriceProvider {

    private List<NetworkEntity> observers;

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
    }

    @Override
    public void handlePacket(Packet packet) {
        Payload payload = packet.getPayload();
        if (payload.type != MessageType.PRICE) {
            return;
        }


    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
