package com.matt.marketsim.entities;

import com.matt.marketsim.Order;
import com.matt.marketsim.events.CallClearingEvent;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class Call extends Exchange {
    private TimeSpan clearingInterval;

    public Call(Model model, String name, SecuritiesInformationProcessor sip, boolean showInTrace, TimeSpan clearingInterval) {
        super(model, name, sip, showInTrace);
        this.clearingInterval = clearingInterval;
        Event<Call> event = new CallClearingEvent(model, "CallClearingEvent", true);
        event.schedule(this, clearingInterval);
    }

    public void clear() {
        //Clearing logic
    }

    @Override
    public void onLimitOrder(Order order) {
        order.setArrivalTime(clock.getTime());
        orderBook.add(order);

        String note = "Handling order: " + order.toString();
        sendTraceNote(note);
    }

    @Override
    public void onCancelOrder(Order order) {

    }

    public TimeSpan getClearingInterval() {
        return clearingInterval;
    }
}
