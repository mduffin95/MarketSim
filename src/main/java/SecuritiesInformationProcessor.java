

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

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
        if (packet.getType() != MessageType.MARKET_UPDATE) {
            return;
        }
        MarketUpdate update = (MarketUpdate) packet.getPayload();
        LOBSummary summary = update.summary;
//        sendTraceNote("SIP quote: BUY = " + quote.getBestBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);

        Order bid = summary.getBestBuyOrder();
        Order offer = summary.getBestSellOrder();
        boolean changed = false;

        //TODO: Refactor
        if (null == bestBid ||
                null != bid &&
                        (bid.getPrice() > bestBid.getPrice() ||
                        (bid.getExchange() == bestBid.getExchange() && bid != bestBid))) {
            bestBid = bid;
            changed = true;
        }
        if (null == bestOffer ||
                null != offer &&
                        (offer.getPrice() < bestOffer.getPrice() ||
                        (offer.getExchange() == bestOffer.getExchange() && offer != bestOffer))) {
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

        LOBSummary summary = new LOBSummary(1);
        summary.buyOrders[0] = bestBid;
        summary.sellOrders[0] = bestOffer;

        //Send updated prices to all observers
        for (NetworkEntity e: observers) {
            e.send(this, MessageType.MARKET_UPDATE, summary, delta);
        }
    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
