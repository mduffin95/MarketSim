package entities;// import the DESMO-J stuff

import desmoj.core.simulator.*;
import entities.NetworkEntity;
import models.MarketSimModel;

public class Exchange extends NetworkEntity {
    MarketSimModel marketSimModel;

    protected Queue<BuyOrder> buyQueue;
    protected Queue<SellOrder> sellQueue;

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
            s.ta.traded(s.price);
            b.ta.traded(s.price);

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

        if (s.price <= b.price) {
            buyQueue.removeFirst();
            b.ta.traded(b.price);
            s.ta.traded(b.price);

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