package com.matt.marketsim;

import com.matt.marketsim.models.ModelExperimentController;
import desmoj.core.report.Reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class TradeStatisticReporter extends Reporter {
    public TradeStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource);

        numColumns = 2;
        columns = new String[numColumns];
        columns[0] = "Title";
//        columns[1] = "Alloc. Efficiency";
//        columns[1] = "Alpha";
        columns[1] = "Total Utility";

        entries = new String[numColumns];
        if (source instanceof TradeStatisticCalculator) {
            entries[0] = source.getName();
//            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getAllocEfficiency());
//            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getSmithsAlpha());
            entries[1] = String.valueOf(((TradeStatisticCalculator) source).getTotalUtility());
        }
        groupHeading = "TradeStatistics";
        groupID = 99;
    }

    @Override
    public String[] getEntries() {
        try {
            String toWrite = entries[0] + ", " + entries[1];
            final Path path = Paths.get(ModelExperimentController.name);
            Files.write(path, Arrays.asList(toWrite), Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {

        }
        return entries;
    }
}
