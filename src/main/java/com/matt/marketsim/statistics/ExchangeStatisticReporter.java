package com.matt.marketsim.statistics;

import com.matt.marketsim.entities.Exchange;
import desmoj.core.report.Reporter;

public class ExchangeStatisticReporter extends Reporter {

    public ExchangeStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 3;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Median Spread";
        columns[2] = "Volatility";

        entries = new String[numColumns];
        if (source instanceof ExchangeStatistics) {
            entries[0] = source.getName();
            entries[1] = String.valueOf(((ExchangeStatistics) source).getMedianSpread());
            entries[2] = String.valueOf(((ExchangeStatistics) source).getVolatility());
        }
        groupHeading = "ExchangeStatistics";
        groupID = 1198;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }
}
