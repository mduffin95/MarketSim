package com.matt.marketsim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import com.matt.marketsim.entities.Call;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class CallClearingEvent extends Event<Call> {
    public CallClearingEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Call call) throws SuspendExecution {
        call.clear();

        this.schedule(call, call.getClearingInterval());
    }
}
