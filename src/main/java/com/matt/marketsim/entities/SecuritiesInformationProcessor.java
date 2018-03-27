package com.matt.marketsim.entities;

import com.matt.marketsim.*;


import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public Optional<OrderTimeStamped> getBestBid() {
        return Optional.ofNullable(bestBid);
    }

    public Optional<OrderTimeStamped> getBestOffer() {
        return Optional.ofNullable(bestOffer);
    }

    public Optional<MarketUpdate> marketUpdateHelper(MarketUpdate update) {
//        sendTraceNote("SIP quote: BUY = " + quote.getBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);

        Objects.requireNonNull(update);

        multiMarketView.add(update);
        Optional<OrderTimeStamped> oldBestBid = getBestBid();
        Optional<OrderTimeStamped> oldBestOffer = getBestOffer();

        Optional<OrderTimeStamped> newBestBid = multiMarketView.getBestBid();
        Optional<OrderTimeStamped> newBestOffer = multiMarketView.getBestOffer();


        if (newBestBid.isPresent() && (!oldBestBid.isPresent() || !newBestBid.get().equals(oldBestBid.get()))) {
            return Optional.of(updateObservers());
        }

        if (newBestOffer.isPresent() && (!oldBestOffer.isPresent() || !newBestOffer.get().equals(oldBestOffer.get()))) {
            return Optional.of(updateObservers());
        }
        return Optional.empty();
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
        if (null == bestBid)
            bestBid = new OrderTimeStamped(presentTime(), null);

        if (null == bestOffer)
            bestOffer = new OrderTimeStamped(presentTime(), null);

        sendTraceNote("NBBO: BUY = " + bestBid.toString() + ", SELL = " + bestOffer.toString());

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
