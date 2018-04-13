package com.matt.marketsim.statistics;

import desmoj.core.report.Reporter;


public class TradeStatisticReporter extends Reporter {
    public TradeStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 5;
        columns = new String[numColumns];
        columns[0] = "Title";
//        columns[1] = "Alloc. Efficiency";
//        columns[1] = "Alpha";
        columns[1] = "Total Utility";
        columns[2] = "Total Execution Time";
        columns[3] = "Total Orders";
        columns[4] = "Inefficiently Traded Orders";

        entries = new String[numColumns];
        if (source instanceof TradeStatistics) {
            entries[0] = source.getName();
//            entries[1] = String.valueOf(((TradeStatistics) source).getAllocEfficiency());
//            entries[1] = String.valueOf(((TradeStatistics) source).getSmithsAlpha());
            entries[1] = String.valueOf(((TradeStatistics) source).getTotalUtility());
            entries[2] = String.valueOf(((TradeStatistics) source).getTotalExecutionTime());
            entries[3] = String.valueOf(((TradeStatistics) source).getTotalOrders());
            entries[4] = String.valueOf(((TradeStatistics) source).getInefficient());
        }
        groupHeading = "TradeStatistics";
        groupID = 99;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }
}
