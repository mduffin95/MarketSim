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

    private OrderTimeStamped bestBid;
    private OrderTimeStamped bestOffer;
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
    public void onLimitOrder(Order order) {

    }

    @Override
    public void onMarketOrder(Order order) {

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
        OrderTimeStamped oldBestBid = bestBid;
        OrderTimeStamped oldBestOffer = bestOffer;
        bestBid = multiMarketView.getBestBid();
        bestOffer = multiMarketView.getBestOffer();

        MarketUpdate m = null;
        if (bestBid != oldBestBid || bestOffer != oldBestOffer) {
            m = updateObservers();
        }
        return m;
    }

    @Override
    public void onCancelOrder(Order order) {

    }

    @Override
    public void onCancelSuccess(Order order) {

    }

    @Override
    public void onCancelFailure(Order order) {

    }

    private MarketUpdate updateObservers() {
        String bidString = bestBid == null ? "none" : String.valueOf(bestBid.order.getPrice());
        String offerString = bestOffer == null ? "none" : String.valueOf(bestOffer.order.getPrice());

        sendTraceNote("NBBO: BUY = " + bidString + ", SELL = " + offerString);

        LOBSummary summary = new LOBSummary(bestBid, bestOffer);

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
