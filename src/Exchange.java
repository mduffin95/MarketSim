import desmoj.core.simulator.*;

import java.util.*;

public class Exchange extends NetworkEntity implements PriceProvider {
    MarketSimModel marketSimModel;

    private PriorityQueue<Payload> buyQueue;
    private PriorityQueue<Payload> sellQueue;

    //Entities that need to be notified of price changes
    private List<NetworkEntity> observers;

    private boolean clearOrdersAfterTrade;
    /**
     * constructs a model...
     */
    public Exchange(Model model, String name, boolean showInTrace, boolean clearOrdersAfterTrade) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        buyQueue = new PriorityQueue<>(10, Comparator.reverseOrder());
        sellQueue = new PriorityQueue<>(10);

        observers = new ArrayList<>();

        this.clearOrdersAfterTrade = clearOrdersAfterTrade;
    }

    @Override
    public void handlePacket(Packet packet) {
        Payload payload = packet.getPayload();
        switch (payload.type) {
            case BUYORDER:
                handleBuyOrder(payload);
                break;
            case SELLORDER:
                handleSellOrder(payload);
                break;
            case PRICE:
                break;
            case CANCEL:
        }
    }

    private void handleBuyOrder(Payload payload) {
        Payload b = payload;

        //Remove worse quotes (there should only ever be one worse)
        for(Payload b1 : buyQueue) {
            if (b1.agent == b.agent) {
                if (b.price > b1.price) {
                    buyQueue.remove(b1);
                    break;
                } else {
                    return; //The new price is not as good as the existing ones
                }
            }
        }

        buyQueue.add(b);
        handleOrder(true);
    }

    private void handleSellOrder(Payload payload) {
        Payload s = payload;

        //Remove worse quotes (there should only ever be one worse)
        for(Payload s1 : sellQueue) {
            if (s1.agent == s.agent) {
                if (s.price < s1.price) {
                    sellQueue.remove(s1);
                    break;
                } else {
                    return;
                }
            }
        }

        sellQueue.add(s);
        handleOrder(false);
    }

    private void handleOrder(boolean buy) {
        Payload b = buyQueue.peek();
        Payload s = sellQueue.peek();

        if (b == null) { return; }
        if (s == null) { return; }

        if (b.price >= s.price) {
            int price;
            if (buy) {
                price = s.price;
            } else {
                price = b.price;
            }
            b.agent.traded(price, true);
            s.agent.traded(price, false);

            marketSimModel.tradePrices.update(price);

            buyQueue.remove(b);
            sellQueue.remove(s);

            //Clear both queues after trade (Gode and Sunder)
            if (clearOrdersAfterTrade) {
                buyQueue.clear();
                sellQueue.clear();
            }
        }
    }

    /**
     * Register a network entity to this exchange. This means the network entity will be sent price updates.
     */
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }


    public void printQueues() {
        System.out.println("Buy Queue");
        while(!buyQueue.isEmpty()){
            System.out.println(buyQueue.poll().price);
        }
        System.out.println("Sell Queue");
        while(!sellQueue.isEmpty()){
            System.out.println(sellQueue.poll().price);
        }
    }

} /* end of model class */