package com.matt.marketsim.entities;

import com.matt.marketsim.*;


import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.TimeSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecuritiesInformationProcessor extends NetworkEntity implements PriceProvider {

    private MultiMarketView multiMarketView;

    private IOrder bestBid;
    private IOrder bestOffer;
    private TimeSpan delta;

    private List<NetworkEntity> observers;

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
        this(model, name, showInTrace, new TimeSpan(0));
    }

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace, TimeSpan delta) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        multiMarketView = new MultiMarketView();
        this.delta = delta;
    }

    @Override
    public void onLimitOrder(IOrder order) {

    }

    @Override
    public void onMarketOrder(IOrder order) {

    }

    @Override
    public void onOwnCompleted(MarketUpdate update) {

    }

    @Override
    public void onMarketUpdate(MarketUpdate update) { //Return value is for testing purposes
        marketUpdateHelper(update);
    }

    public MarketUpdate marketUpdateHelper(MarketUpdate update) {
//        sendTraceNote("SIP quote: BUY = " + quote.getBestBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);


        multiMarketView.add(update);
        IOrder oldBestBid = bestBid;
        IOrder oldBestOffer = bestOffer;
        bestBid = multiMarketView.getBestBid();
        bestOffer = multiMarketView.getBestOffer();

        MarketUpdate m = null;
        if (bestBid != oldBestBid || bestOffer != oldBestOffer) {
            m = updateObservers();
        }
        return m;
    }

    @Override
    public void onCancelOrder(IOrder order) {

    }

    @Override
    public void onCancelSuccess(IOrder order) {

    }

    @Override
    public void onCancelFailure(IOrder order) {

    }

    private MarketUpdate updateObservers() {
        String bidString = bestBid == null ? "none" : String.valueOf(bestBid.getPrice());
        String offerString = bestOffer == null ? "none" : String.valueOf(bestOffer.getPrice());

        sendTraceNote("NBBO: BUY = " + bidString + ", SELL = " + offerString);

        LOBSummary summary = new LOBSummary(1);
        summary.buyOrders[0] = bestBid;
        summary.sellOrders[0] = bestOffer;

        MarketUpdate m = new MarketUpdate(this, null, summary);

        //Send updated prices to all observers, adding delta delay before sending
        for (NetworkEntity e: observers) {
            e.send(this, MessageType.MARKET_UPDATE, m, delta);
        }

        return m;
    }

    @Override
    public void registerPriceObserver(NetworkEntity networkEntity) {
        observers.add(networkEntity);
    }
}
