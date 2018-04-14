package com.matt.marketsim.statistics;

import desmoj.core.report.Reporter;

public class RoutingStatisticReporter extends Reporter {

    public RoutingStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 4;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Inefficient Routings";
        columns[2] = "Total Orders";
        columns[3] = "Inefficient Fraction";

        entries = new String[numColumns];
        if (source instanceof RoutingStatistics) {
            entries[0] = source.getName();
            entries[1] = String.valueOf(((RoutingStatistics) source).getInefficient());
            entries[2] = String.valueOf(((RoutingStatistics) source).getTotalOrders());
            entries[3] = String.valueOf(((RoutingStatistics) source).getInefficientFraction());
        }
        groupHeading = "RouteStatistics";
        groupID = 1298;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }
}
