package com.matt.marketsim.entities;

import com.matt.marketsim.*;


import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.TimeSeries;

import java.util.ArrayList;
import java.util.List;

public class SecuritiesInformationProcessor extends NetworkEntity implements PriceProvider {

    private Order bestBid;
    private Order bestOffer;
    private TimeSpan delta;

    private List<NetworkEntity> observers;

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
        this(model, name, showInTrace, new TimeSpan(0));
    }

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace, TimeSpan delta) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        this.delta = delta;
    }

    @Override
    protected void onLimitOrder(Order order) {

    }

    @Override
    protected void onMarketOrder(Order order) {

    }

    @Override
    protected void onOwnCompleted(MarketUpdate update) {

    }

    @Override
    protected void onMarketUpdate(MarketUpdate update) {
        LOBSummary summary = update.summary;
//        sendTraceNote("SIP quote: BUY = " + quote.getBestBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);

        Order bid = summary.getBestBuyOrder();
        Order offer = summary.getBestSellOrder();
        boolean changed = false;

        //TODO: Refactor
        if ((null == bestBid ^ null == bid) ||
                null != bid &&
                        (bid.getPrice() > bestBid.getPrice() ||
                                (bid.getExchange() == bestBid.getExchange() && bid != bestBid))) {
            bestBid = bid;
            changed = true;
        }
        if ((null == bestOffer ^ null == offer) ||
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

    @Override
    protected void onCancelOrder(Order order) {

    }

    @Override
    protected void onCancelSuccess(Order order) {

    }

    @Override
    protected void onCancelFailure(Order order) {

    }

    private void updateObservers() {
        String bidString = bestBid == null ? "none" : String.valueOf(bestBid.getPrice());
        String offerString = bestOffer == null ? "none" : String.valueOf(bestOffer.getPrice());

        sendTraceNote("NBBO: BUY = " + bidString + ", SELL = " + offerString);

        LOBSummary summary = new LOBSummary(1);
        summary.buyOrders[0] = bestBid;
        summary.sellOrders[0] = bestOffer;

        MarketUpdate m = new MarketUpdate(null, summary);

        //Send updated prices to all observers, adding delta delay before sending
        for (NetworkEntity e: observers) {
            e.send(this, MessageType.MARKET_UPDATE, m, delta);
        }
    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
