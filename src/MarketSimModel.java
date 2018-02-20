import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.TimeSeries;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MarketSimModel extends Model {

    protected Random generator;
    private DistributionManager distributionManager;

    // define model components here
    //Random number stream used to draw an arrival time for the next trading agent
    private ContDistExponential agentArrivalTime;
    private ContDistUniform agentArrivalTimeUniform;

    //Random number stream used to draw a limit price for the next trading agent
    private DiscreteDistUniform priceDist;

    //Random boolean used to determine whether agent is buying or selling
    private BoolDistBernoulli buyOrSell;

    protected TimeSeries tradePrices;
    protected long totalUtility;
    protected long theoreticalUtility;

    public static int MIN_PRICE = 1;
    public static int MAX_PRICE = 200;
    //    public static int NUM_TRADERS = 10;
    public static int MEAN_TIME_BETWEEN_TRADES = 10;
    public static int SIM_LENGTH = 250;
    public static int EQUILIBRIUM = 100;
    public static boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = false;


    //TODO: Extend this to a list of exchanges
    private Exchange exchange;
    private SecuritiesInformationProcessor sip;


    public MarketSimModel() {
        super(null, "ExchangeModel", true, true);
        generator = new Random();
    }

    @Override
    public String description() {
        return "Market simulator model.";
    }

    @Override
    public void doInitialSchedules() {

        //Create the supply and demand curves
        for (int i = 0; i < 25; i++) {
            TradingAgent agentBuy = new ZIC(this, 40 + i * 5, exchange, sip, Direction.BUY);
            TradingAgent agentSell = new ZIC(this, 40 + i * 5, exchange, sip, Direction.SELL);

            TradingAgentDecisionEvent buy = new TradingAgentDecisionEvent(this, "BuyDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);
            TradingAgentDecisionEvent sell = new TradingAgentDecisionEvent(this, "SellDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);

            buy.schedule(agentBuy, getAgentArrivalTime());
            sell.schedule(agentSell, getAgentArrivalTime());
        }
    }

    @Override
    public void init() {

        long seed = generator.nextLong();
        distributionManager = new DistributionManager("Distribution Manager", seed);


        //Distributions
        agentArrivalTime = new ContDistExponential(this, "AgentArrivalTimeStream",
                MEAN_TIME_BETWEEN_TRADES, true, false);
        agentArrivalTime.setNonNegative(true);

        agentArrivalTimeUniform = new ContDistUniform(this, "AgentArrivalTimeUniformStream",
                0, 100, true, false);

        priceDist = new DiscreteDistUniform(this, "LimitPriceStream", MIN_PRICE, MAX_PRICE,
                true, false);

        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        distributionManager.register(agentArrivalTime);
        distributionManager.register(agentArrivalTimeUniform);
        distributionManager.register(priceDist);
        distributionManager.register(buyOrSell);

        //Entities
        sip = new SecuritiesInformationProcessor(this, "Securities Information Processor", MarketSimModel.SHOW_ENTITIES_IN_TRACE);
        exchange = new Exchange(this, "Exchange", sip, MarketSimModel.SHOW_ENTITIES_IN_TRACE);


        //Reporting
        tradePrices = new TimeSeries(this, "Trade prices over time", "trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);

//        totalUtility = new Tally(this, "Utility Tally", true, false);
//        theoreticalUtility = new Tally(this, "Theoretical Utility Tally", false, false);
    }


    public TimeSpan getAgentArrivalTime() {
//        return agentArrivalTime.sample();
        return new TimeSpan(agentArrivalTime.sample(), TimeUnit.SECONDS);
    }

    public int getRandomPrice() {
        return priceDist.sample().intValue();
    }

    public boolean getBuyOrSell() {
        return buyOrSell.sample();
    }

    public TimeSpan getLatency(NetworkEntity a, NetworkEntity b) {
        //TODO: Implement adjacency matrix
        return new TimeSpan(0, TimeUnit.MICROSECONDS);
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
        exp.setShowProgressBar(false);
        TimeInstant stopTime = new TimeInstant(MarketSimModel.SIM_LENGTH, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);
        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();

        System.out.println("Total Utility = " + model.totalUtility);
        System.out.println("Theoretical Total Utility = " + model.theoreticalUtility);
        double allocative_efficiency = model.totalUtility / (double) model.theoreticalUtility;

        System.out.println("Allocative Efficiency = " + allocative_efficiency);

        model.exchange.printQueues();
    }
}
