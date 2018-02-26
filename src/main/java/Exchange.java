import desmoj.core.simulator.*;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {
    private MarketSimModel marketSimModel;

    private OrderBook orderBook;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    //For testing purposes
    public Trade recentTrade;

    public Exchange(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        observers = new ArrayList<>();
        orderBook = new OrderBook();

        registerPriceObserver(sip);
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
            case MARKET_UPDATE:
                break;
            case CANCEL:
                order = (Order)packet.getPayload();
                orderBook.remove(order);
                break;
        }
    }

    private void handleOrder(Order order) {
        LOBSummary original = orderBook.getSummary(1);
        orderBook.add(order);

        Order b = orderBook.getBestBuyOrder();
        Order s = orderBook.getBestSellOrder();

        Trade newTrade = null;

        if (null != b && null != s && b.getPrice() >= s.getPrice()) {
            int price;
            if (order.direction == Direction.BUY) {
                price = s.getPrice();
            } else {
                price = b.getPrice();
            }
            TimeInstant currentTime = marketSimModel.getExperiment().getSimClock().getTime();
            newTrade = new Trade(currentTime, price, 1, b.agent, s.agent);

            //Record the trade
            marketSimModel.tradePrices.update(price);
            sendTraceNote("Trade at " + price);

            //Remove from the order book
            orderBook.pollBestBuyOrder();
            orderBook.pollBestSellOrder();
        }

        LOBSummary newSummary = orderBook.getSummary(1);
        if (newSummary.getBestBuyOrder() != original.getBestBuyOrder() ||
                newSummary.getBestSellOrder() != original.getBestSellOrder() ||
                newTrade != null) {
            MarketUpdate update = new MarketUpdate(newTrade, newSummary);
            MessageType msg;

            //The price quote has changed so this needs to be sent to all observers
            msg = MessageType.MARKET_UPDATE;

            for (NetworkEntity e: observers) {
                e.send(this, msg, update, marketSimModel.getLatency(this, e));
            }
        }
        recentTrade = newTrade;
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

    public OrderBook getOrderBook() {
        return orderBook;
    }
} /* end of model class */