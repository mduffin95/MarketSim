package com.matt.marketsim.models;

import desmoj.core.simulator.Model;

public class DummyModel extends Model {

    public DummyModel() {
        super(null, "DummyModel", false, false);
    }

    @Override
    public String description() {
        return "This is a dummy model.";
    }

    @Override
    public void doInitialSchedules() {

    }

    @Override
    public void init() {

    }
}
