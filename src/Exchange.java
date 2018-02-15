import com.sun.org.apache.xpath.internal.operations.Or;
import desmoj.core.simulator.*;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {
    private MarketSimModel marketSimModel;
    private SecuritiesInformationProcessor sip;

    private OrderBook orderBook;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    private boolean clearOrdersAfterTrade;
    /**
     * constructs a model...
     */
    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace, boolean clearOrdersAfterTrade) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        this.sip = sip;

        observers = new ArrayList<>();

        this.clearOrdersAfterTrade = clearOrdersAfterTrade;

        orderBook = new OrderBook();
    }

    @Override
    public void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case LIMIT_ORDER:
                Order order = (Order)packet.getPayload();
                handleOrder(order);
                break;
            case MARKET_ORDER:
                break;
            case PRICE_QUOTE:
                break;
            case CANCEL:
        }
    }

    private void handleOrder(Order order) {
        orderBook.add(order);

        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();

        if (b == null) { return; }
        if (s == null) { return; }

        if (b.price >= s.price) {
            int price;
            if (order.direction == Direction.BUY) {
                price = s.price;
            } else {
                price = b.price;
            }
            b.agent.traded(price, Direction.BUY);
            s.agent.traded(price, Direction.SELL);

            marketSimModel.tradePrices.update(price);

            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();

            //Clear both queues after trade (Gode and Sunder)
            if (clearOrdersAfterTrade) {
                orderBook.clear();
            }

            //Update the SIP with the new best bid and offer prices
            PriceQuote payload = orderBook.getPriceQuote(1);
            sip.send(this, MessageType.PRICE_QUOTE, payload);
        }
    }

    /**
     * Register a network entity to this exchange. This means the network entity will be sent price updates.
     */
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }


    public void printQueues() {
        orderBook.printOrderBook();
    }

} /* end of model class */