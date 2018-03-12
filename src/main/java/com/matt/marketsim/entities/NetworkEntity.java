package com.matt.marketsim.entities;

import com.matt.marketsim.*;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.*;
import com.matt.marketsim.events.PacketSendEvent;

public abstract class NetworkEntity extends Entity {
    MarketSimModel marketSimModel;

    public NetworkEntity(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        marketSimModel = (MarketSimModel) model;
    }

    //Used to handle incoming packets
    public void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case LIMIT_ORDER:
                onLimitOrder((Order) packet.getPayload());
                break;
            case MARKET_ORDER:
                onMarketOrder((Order) packet.getPayload());
                break;
            case MARKET_UPDATE: //Determine whether it is my trade
                MarketUpdate update = (MarketUpdate)packet.getPayload();

                if (isMyTrade(update.trade)) {
                    onOwnCompleted(update);
                } else {
                    onMarketUpdate(update);
                }
                break;
            case CANCEL:
                onCancelOrder((Order) packet.getPayload());
                break;
            case CANCEL_SUCCESS:
                onCancelSuccess((Order) packet.getPayload());
                break;
            case CANCEL_FAILURE:
                onCancelFailure((Order) packet.getPayload());
                break;
        }
    }

    boolean isMyTrade(Trade trade) {
        return null != trade && (this == trade.buyer || this == trade.seller);
    }

    //Send a payload to this NetworkEntity from the source NetworkEntity
    public void send(NetworkEntity source, MessageType type, Object payload) {
        send(source, type, payload, new TimeSpan(0));
    }

    public void send(NetworkEntity source, MessageType type, Object payload, TimeSpan delay) {
        String name = "PacketFrom" + source.getName() + "To" + this.getName();
        Packet packet = new Packet(marketSimModel, name, true, source, this, type, payload);

        PacketSendEvent sendEvent = new PacketSendEvent(marketSimModel);
        sendEvent.schedule(packet, delay);
    }

    protected abstract void onLimitOrder(Order order);
    protected abstract void onMarketOrder(Order order);
    protected abstract void onOwnCompleted(MarketUpdate update);
    protected abstract void onMarketUpdate(MarketUpdate update);
    protected abstract void onCancelOrder(Order order);
    protected abstract void onCancelSuccess(Order order);
    protected abstract void onCancelFailure(Order order);
}