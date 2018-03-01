package com.matt.marketsim;

import desmoj.core.report.Reporter;

public class TradeStatisticReporter extends Reporter {
    public TradeStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 3;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Alloc. Efficiency";
        columns[2] = "Alpha";

        entries = new String[numColumns];
        if (source instanceof TradeStatisticCalculator) {
            entries[0] = source.getName();
            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getAllocEfficiency());
            entries[2] = String.valueOf(((TradeStatisticCalculator) source).getSmithsAlpha());
        }
        groupHeading = "TradeStatistics";
        groupID = 99;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }
}
