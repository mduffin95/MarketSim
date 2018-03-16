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

    private Map<Exchange, LOBSummary> summaryMap;

    private Order bestBid;
    private Order bestOffer;
    private TimeSpan delta;

    private List<NetworkEntity> observers;

//    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace) {
//        this(model, name, showInTrace, new TimeSpan(0));
//    }

    public SecuritiesInformationProcessor(Model model, String name, boolean showInTrace, TimeSpan delta) {
        super(model, name, showInTrace);
        observers = new ArrayList<>();
        summaryMap = new HashMap<>();
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

        //TODO: Need to have access to the exchange in a better way than this.
        Exchange e;
        Order o = summary.getBestBuyOrder();
        e = null != o ? o.getExchange() : null;
        o = summary.getBestSellOrder();
        if (null == e && null != o) {
            e = o.getExchange();
        }

        summaryMap.put(e, summary);
        Order oldBestBid = bestBid;
        Order oldBestOffer = bestOffer;
        bestBid = null;
        bestOffer = null;

        for (Map.Entry<Exchange, LOBSummary> entry : summaryMap.entrySet()) {
            Order bid = entry.getValue().getBestBuyOrder();
            Order offer = entry.getValue().getBestSellOrder();
            if (null == bestBid || bid != null && bid.getPrice() > bestBid.getPrice()) {
                bestBid = bid;
            }
            if (null == bestOffer || offer != null && offer.getPrice() < bestOffer.getPrice()) {
                bestOffer = offer;
            }
        }
        if (bestBid != oldBestBid || bestOffer != oldBestOffer) {
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
