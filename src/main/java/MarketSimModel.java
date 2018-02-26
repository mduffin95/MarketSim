import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.TimeSeries;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MarketSimModel extends Model {

    /*
     * Distributions and number generators
     */
    protected Random generator;
    private ContDistExponential agentArrivalTime;
    private ContDistUniform agentArrivalTimeUniform;
    private DiscreteDistUniform priceDist;
    private BoolDistBernoulli buyOrSell;
    private DistributionManager distributionManager;

    /*
     * Metrics for reporting
     */
    protected TimeSeries tradePrices;
    protected long totalUtility;
    protected long theoreticalUtility;

    private NetworkBuilder builder;


    /*
     * Model parameters
     */

    public static int MIN_PRICE = 1;
    public static int MAX_PRICE = 200;
    public static int MEAN_TIME_BETWEEN_TRADES = 10;
    public static int SIM_LENGTH = 100;
    public static int EQUILIBRIUM = 100;
    public static boolean SHOW_ENTITIES_IN_TRACE = true;
    public static boolean SHOW_EVENTS_IN_TRACE = false;

    /*
     * Model entities
     */
    private SecuritiesInformationProcessor sip;
    private ArrayList<TradingAgent> agents;
    private ArrayList<Exchange> exchanges;


    /*
     * For testing purposes
     */
//    public boolean testing = false;
//    public ArrayList<Packet> packets = new ArrayList<>();

    /*
     * Methods
     */


    public MarketSimModel(NetworkBuilder builder) {
        super(null, "ExchangeModel", true, true);
        generator = new Random();
        this.builder = builder;
    }

    @Override
    public String description() {
        return "Market simulator model.";
    }

    @Override
    public void doInitialSchedules() {
        for (TradingAgent a: agents) {
            TradingAgentDecisionEvent event = new TradingAgentDecisionEvent(this, "MarketEntryDecision", MarketSimModel.SHOW_EVENTS_IN_TRACE);
            event.schedule(a, getAgentArrivalTime());
        }
    }

    @Override
    public void init() {

        /*
         * Distributions and number generators
         */
        long seed = generator.nextLong();
        distributionManager = new DistributionManager("Distribution Manager", seed);
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

        /*
         * Entities
         */
        agents = new ArrayList<>();
        exchanges = new ArrayList<>();
        builder.createNetworkEntities(this, agents, exchanges, sip);


        /*
         * Reporting
         */
        tradePrices = new TimeSeries(this, "Trade prices over time", "trade_prices.txt",
                new TimeInstant(0.0), new TimeInstant(MarketSimModel.SIM_LENGTH), true, false);

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

    public Exchange getExchange() {
        return exchanges.get(0);
    }

    public long getTotalUtility() {
        return totalUtility;
    }

    public long getTheoreticalUtility() {
        return theoreticalUtility;
    }


    /**
     * runs the model
     */
    public static void main(String[] args) {

        // create model and experiment
        Experiment exp = new Experiment("Exp1");
        NetworkBuilder builder = new ZIPExperiment(25, 0, 200);
        MarketSimModel model = new MarketSimModel(builder);
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

        model.getExchange().printQueues();
    }
}
