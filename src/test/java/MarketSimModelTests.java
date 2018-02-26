import com.matt.marketsim.builders.NetworkBuilder;
import com.matt.marketsim.builders.ZIPExperiment;
import desmoj.core.simulator.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.models.TestModel;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
/*
 * Use this class to test a number of different model setups.
 */
public class MarketSimModelTests {

    @Test
    void runSimulation() {
        NetworkBuilder builder = new ZIPExperiment(25, 40, 165);
        TestModel model = new TestModel(builder);
        Experiment exp = new Experiment("Exp1");
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(MarketSimModel.SIM_LENGTH, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();

        System.out.println("Total Utility = " + model.getTotalUtility());
        System.out.println("Theoretical Total Utility = " + model.getTheoreticalUtility());
        double allocative_efficiency = model.getTotalUtility() / (double) model.getTheoreticalUtility();

        System.out.println("Allocative Efficiency = " + allocative_efficiency);

//        model.getExchange().printQueues();

        final File actualTrades = new File("trade_prices.txt");
        final File expectedTrades = new File("src/test/resources/trade_prices_1.txt");

        BufferedReader expectedTradesReader = null;
        BufferedReader actualTradesReader = null;

        try {
            expectedTradesReader = new BufferedReader(new FileReader(expectedTrades));
            actualTradesReader = new BufferedReader(new FileReader(actualTrades));
            String expectedLine;
            String actualLine;
            while ((expectedLine = expectedTradesReader.readLine()) != null) {
                actualLine = actualTradesReader.readLine();
                assertNotNull(actualLine, "Expected had more trades.");
                assertEquals(expectedLine, actualLine);
            }
            assertNull(actualTradesReader.readLine(), "Test file had more trades.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}