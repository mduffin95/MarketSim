import desmoj.core.simulator.*;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {
    private MarketSimModel marketSimModel;
    private SecuritiesInformationProcessor sip;

    private OrderBook orderBook;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    /**
     * constructs a model...
     */
    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        this.sip = sip;

        observers = new ArrayList<>();

        orderBook = new OrderBook();
    }

    @Override
    public void handlePacket(Packet packet) {
        Order order;
        switch (packet.getType()) {
            case LIMIT_ORDER:
                order = (Order)packet.getPayload();
                handleOrder(order);
                break;
            case MARKET_ORDER:
                break;
            case PRICE_QUOTE:
                break;
            case CANCEL:
                order = (Order)packet.getPayload();
                orderBook.remove(order);
                break;
        }
    }

    private void handleOrder(Order order) {
        PriceQuote original = orderBook.getPriceQuote(1);
        orderBook.add(order);

        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();

        if (b == null) { return; }
        if (s == null) { return; }

        if (b.getPrice() >= s.getPrice()) {
            int price;
            if (order.direction == Direction.BUY) {
                price = s.getPrice();
            } else {
                price = b.getPrice();
            }
            b.agent.traded(price, Direction.BUY);
            s.agent.traded(price, Direction.SELL);

            //Record the trade
            marketSimModel.tradePrices.update(price);
            sendTraceNote("Trade at " + price);

            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
        }

        //The price quote has changed so this needs to be sent to the SIP
        PriceQuote newQuote = orderBook.getPriceQuote(1);
        if (newQuote.getBestBuyOrder() != original.getBestBuyOrder() ||
                newQuote.getBestSellOrder() != original.getBestSellOrder()) {
            sip.send(this, MessageType.PRICE_QUOTE, newQuote);
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