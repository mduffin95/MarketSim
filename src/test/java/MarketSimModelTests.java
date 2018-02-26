import desmoj.core.simulator.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;

/*
 * Use this class to test a number of different model setups.
 */
public class MarketSimModelTests {

    @Test
    void runSimulation() {
        NetworkBuilder builder = new ZIPExperiment(25, 40, 165);
        MarketSimModel model = new MarketSimModel(builder);
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

        model.getExchange().printQueues();
    }

}
