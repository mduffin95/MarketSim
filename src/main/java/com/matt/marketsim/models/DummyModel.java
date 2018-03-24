package com.matt.marketsim.models;

import com.matt.marketsim.WellmanGraph;
import com.matt.marketsim.dtos.ResultDto;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class DummyModel extends MarketSimModel {
    public DummyModel() {
        super(null, "DummyModel", false, false, 0);
    }

    @Override
    public String description() {
        return "This is a dummy model.";
    }

    @Override
    public void doInitialSchedules() {

    }

    @Override
    public TimeSpan getAgentArrivalTime() {
        return null;
    }

    @Override
    protected WellmanGraph getNetwork() {
        return null;
    }

    @Override
    public boolean showPacketSendInTrace() {
        return false;
    }

    @Override
    public boolean showPacketArrivalInTrace() {
        return false;
    }

    @Override
    WellmanGraph createNetwork() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public ResultDto getResults() {
        return null;
    }
}
