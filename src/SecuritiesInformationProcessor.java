import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

public class SecuritiesInformationProcessor extends NetworkEntity implements PriceProvider {

    private int bid;
    private int offer;
    private TimeSpan delta;

    private List<NetworkEntity> observers;

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
    }

    @Override
    public void handlePacket(Packet packet) {
        if (packet.getType() != MessageType.PRICE_QUOTE) {
            return;
        }

        PriceQuote quote = (PriceQuote) packet.getPayload();
        sendTraceNote("SIP quote: BUY = " + quote.getBestBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);
    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
