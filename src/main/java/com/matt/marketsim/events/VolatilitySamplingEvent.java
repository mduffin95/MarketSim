package com.matt.marketsim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import com.matt.marketsim.entities.Call;
import com.matt.marketsim.entities.Exchange;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class VolatilitySamplingEvent extends Event<Exchange> {
    public VolatilitySamplingEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Exchange exchange) throws SuspendExecution {
        if (presentTime().getTimeAsDouble() > 3000)
            return;

        exchange.updateVolatilityStats();
        this.schedule(exchange, exchange.getVolatilitySamplingInterval());
    }
}
