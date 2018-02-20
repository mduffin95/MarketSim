import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.awt.print.PrinterException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class SecuritiesInformationProcessor extends NetworkEntity implements PriceProvider {

    private Order bestBid;
    private Order bestOffer;
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
//        sendTraceNote("SIP quote: BUY = " + quote.getBestBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);

        Order bid = quote.getBestBuyOrder();
        Order offer = quote.getBestSellOrder();
        boolean changed = false;

        if (bestBid == null || bid.getPrice() > bestBid.getPrice() ||
                (bid.getExchange() == bestBid.getExchange() && bid != bestBid)) {
            bestBid = bid;
            changed = true;
        }
        if (bestOffer == null || offer.getPrice() < bestOffer.getPrice() ||
                (offer.getExchange() == bestOffer.getExchange() && offer != bestOffer)) {
            bestOffer = offer;
            changed = true;
        }
        if (changed) {
            updateObservers();
        }
    }

    private void updateObservers() {
        String bidString = bestBid == null ? "none" : String.valueOf(bestBid.getPrice());
        String offerString = bestOffer == null ? "none" : String.valueOf(bestOffer.getPrice());

        sendTraceNote("NBBO: BUY = " + bidString + ", SELL = " + offerString);

        PriceQuote priceQuote = new PriceQuote(1);
        priceQuote.buyOrders[0] = bestBid;
        priceQuote.sellOrders[0] = bestOffer;

        //Send updated prices to all observers
        for (NetworkEntity e: observers) {
            e.send(this, MessageType.PRICE_QUOTE, priceQuote, delta);
        }
    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
