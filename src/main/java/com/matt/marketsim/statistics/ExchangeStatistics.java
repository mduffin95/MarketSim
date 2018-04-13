package com.matt.marketsim.statistics;

import desmoj.core.simulator.Model;
import desmoj.core.statistic.StatisticObject;

import java.util.Observable;

public class ExchangeStatistics extends StatisticObject {
    public ExchangeStatistics(Model model, String name, boolean showInReport, boolean showInTrace) {
        super(model, name, showInReport, showInTrace);
    }

    @Override
    public void update(Observable observable, Object o) {

    }
}
