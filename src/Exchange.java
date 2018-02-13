import desmoj.core.simulator.*;

public class Exchange extends NetworkEntity {
    MarketSimModel marketSimModel;

    protected Queue<BuyOrder> buyQueue;
    protected Queue<SellOrder> sellQueue;

    //Entities that need to be notified of price changes
    protected Queue<NetworkEntity> observers;
    /**
     * constructs a model...
     */
    public Exchange(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel)getModel();
        buyQueue = new Queue<>(marketSimModel, "BuyQueue", true, true);
        sellQueue = new Queue<>(marketSimModel, "SellQueue", true, true);
        observers = new Queue<>(marketSimModel, "Observers", true, true);
    }

    @Override
    public void handleBuyOrder(BuyOrder buyOrder) {
        BuyOrder b = buyOrder;
        SellOrder s = sellQueue.first();

        if (s == null) {
            buyQueue.insert(b);
            return;
        }

        if (b.price >= s.price) {
            sellQueue.removeFirst();
            s.agent.traded(s.price);
            b.agent.traded(s.price);
            marketSimModel.tradePrices.update(s.price);

            //Clear both queues after trade (Gode and Sunder)
            buyQueue.removeAll();
            sellQueue.removeAll();

        } else {
            buyQueue.insert(b);
        }
    }

    @Override
    public void handleSellOrder(SellOrder sellOrder) {
        BuyOrder b = buyQueue.first();
        SellOrder s = sellOrder;

        if (b == null) {
            sellQueue.insert(s);
            return;
        }

        if (b.price >= s.price) {
            b.agent.traded(b.price);
            s.agent.traded(b.price);
            marketSimModel.tradePrices.update(b.price);

            //Clear both queues after trade (Gode and Sunder)
            buyQueue.removeAll();
            sellQueue.removeAll();

        } else {
            sellQueue.insert(s);
        }
    }

    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        //TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Register a trading agent to this exchange. This means this exchange is now the primary exchange
     * for that trading agent.
     */
    public void registerPrimary(TradingAgent agent) {
        agent.primaryExchange = this;
        observers.insert(agent);
    }
} /* end of model class */