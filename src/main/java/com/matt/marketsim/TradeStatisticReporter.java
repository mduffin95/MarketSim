package com.matt.marketsim;

import desmoj.core.report.Reporter;


public class TradeStatisticReporter extends Reporter {
    public TradeStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 4;
        columns = new String[numColumns];
        columns[0] = "Title";
//        columns[1] = "Alloc. Efficiency";
//        columns[1] = "Alpha";
        columns[1] = "Total Utility";
        columns[2] = "Total Execution Time";
        columns[3] = "Total Orders";

        entries = new String[numColumns];
        if (source instanceof TradeStatisticCalculator) {
            entries[0] = source.getName();
//            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getAllocEfficiency());
//            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getSmithsAlpha());
            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getTotalUtility());
            entries[2] = String.valueOf(((TradeStatisticCalculator) source).getTotalExecutionTime());
            entries[3] = String.valueOf(((TradeStatisticCalculator) source).getTotalOrders());
        }
        groupHeading = "TradeStatistics";
        groupID = 99;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }
}
