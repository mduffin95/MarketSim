package com.matt.marketsim.entities;

import com.matt.marketsim.*;


import com.matt.marketsim.events.VolatilitySamplingEvent;
import com.matt.marketsim.statistics.ExchangeStatistics;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
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

    public ExchangeStatistics stats;

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
        this(model, name, showInTrace, new TimeSpan(0));
    }

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace, TimeSpan delta) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        multiMarketView = new MultiMarketView();
        this.delta = delta;

        stats = new ExchangeStatistics(model, "SIPStats", true, false);
//        volatilitySamplingInterval = new TimeSpan(250);
//        Event<Exchange> event = new VolatilitySamplingEvent(model, "VolatilitySamplingEvent", true);
//        event.schedule(this, volatilitySamplingInterval);
    }

    void updateSpreadStats() {
        if (presentTime().getTimeAsDouble() > 3000)
            return;

        LOBSummary summary = new LOBSummary(bestBid, bestOffer);
        stats.spreadUpdate(summary);
    }

//    public void updateVolatilityStats() {
//        TimeInstant t = presentTime();
//        if (t.getTimeAsDouble() > 3000)
//            return;
//
//        stats.volatilityUpdate(orderBook.getSummary(clock));
//    }

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

    public Optional<MarketUpdate> marketUpdateHelper(MarketUpdate update) {
//        sendTraceNote("SIP quote: BUY = " + quote.getBuyOrder().price + ", SELL = " + quote.getBestSellOrder().price);

        Objects.requireNonNull(update);

        multiMarketView.add(update);

        Optional<OrderTimeStamped> bidOpt = multiMarketView.getBestBid();
        Optional<OrderTimeStamped> offerOpt = multiMarketView.getBestOffer();

        OrderTimeStamped newBestBid = bidOpt.orElse(new OrderTimeStamped(presentTime(), null));
        OrderTimeStamped newBestOffer = offerOpt.orElse(new OrderTimeStamped(presentTime(), null));

        //TODO: If there is no newBestBid or newBestOffer we incorrectly do not update the observers.
        boolean changed = false;
        if (null == bestBid || !newBestBid.equals(bestBid)) {
            bestBid = newBestBid;
            changed = true;
        }

        if (null == bestOffer || !newBestOffer.equals(bestOffer)) {
            bestOffer = newBestOffer;
            changed = true;
        }
        if (changed) {
            updateSpreadStats();
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

        String bidString = (!bestBid.getOrder().isPresent()) ? "none" : String.valueOf(bestBid.getOrder().get().getPrice());
        String offerString = (!bestOffer.getOrder().isPresent()) ? "none" : String.valueOf(bestOffer.getOrder().get().getPrice());

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
