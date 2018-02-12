import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.dist.DistributionManager;
import desmoj.core.simulator.*;
import desmoj.core.statistic.TimeSeries;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MarketSimModel extends Model {

    protected DistributionManager distributionManager;

    // define model components here
    //Random number stream used to draw an arrival time for the next trading agent
    private ContDistExponential agentArrivalTime;

    //Random number stream used to draw a limit price for the next trading agent
    private DiscreteDistUniform limitPrice;

    //Random boolean used to determine whether agent is buying or selling
    private BoolDistBernoulli buyOrSell;

    protected TimeSeries tradePrices;

    public static int MIN_PRICE = 1;
    public static int MAX_PRICE = 200;
    public static int NUM_TRADERS = 10;
    public static int SIM_LENGTH = 1440;

    //TODO: Extend this to a list of exchanges
    public Exchange exchange;

    public MarketSimModel() {
        super(null, "ExchangeModel", true, true);

    }

    @Override
    public String description() {
        return "Market simulator model.";
    }

    @Override
    public void doInitialSchedules() {
        TradingAgentGeneratorEvent generator = new TradingAgentGeneratorEvent(this,
                "TradingAgentGenerator", true);

        generator.schedule(new TimeSpan(0, TimeUnit.SECONDS));
    }

    @Override
    public void init() {
        Random generator = new Random();
        long seed = generator.nextLong();
        distributionManager = new DistributionManager("Distribution Manager", seed);


        //Distributions
        agentArrivalTime = new ContDistExponential(this, "AgentArrivalTimeStream", 3, true, false);
        agentArrivalTime.setNonNegative(true);

        limitPrice = new DiscreteDistUniform(this, "LimitPriceStream", MIN_PRICE, MAX_PRICE,
                true, false);

        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        distributionManager.register(agentArrivalTime);
        distributionManager.register(limitPrice);
        distributionManager.register(buyOrSell);

        //Entities
        exchange = new Exchange(this, "Exchange", true);


        //Reporting
        tradePrices = new TimeSeries(this, "Trade prices over time", "trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);
    }


    public double getAgentArrivalTime() {
        return agentArrivalTime.sample();
    }

    public int getLimitPrice() {
        return limitPrice.sample().intValue();
    }

    public boolean getBuyOrSell() {
        return buyOrSell.sample();
    }

    public long getLatency(Entity a, Entity b) {
        //TODO: Implement adjacency matrix
        return 0;
    }

    /**
     * runs the model
     */
    public static void main(String[] args) {

        // create model and experiment
        MarketSimModel model = new MarketSimModel();
        Experiment exp = new Experiment("Exp1");
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(true);
        TimeInstant stopTime = new TimeInstant(MarketSimModel.SIM_LENGTH, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }

}
