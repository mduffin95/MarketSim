// import the DESMO-J stuff

import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;

import java.util.concurrent.TimeUnit;

public class ExchangeModel extends Model {

    // define model components here
    //Random number stream used to draw an arrival time for the next trading agent
    private ContDistExponential agentArrivalTime;

    //Random number stream used to draw a limit price for the next trading agent
    private DiscreteDistUniform limitPrice;

    //Random boolean used to determine whether agent is buying or selling
    private BoolDistBernoulli buyOrSell;

    protected Queue<BuyOrder> buyQueue;
    protected Queue<SellOrder> sellQueue;


    protected static int MIN_PRICE = 10;
    protected static int MAX_PRICE = 1000;

    /**
     * constructs a model...
     */
    public ExchangeModel() {
        super(null, "ExchangeModel", true, true);

    }

    /**
     * initialise static components
     */
    public void init() {
        agentArrivalTime = new ContDistExponential(this, "AgentArrivalTimeStream", 3, true, false);
        agentArrivalTime.setNonNegative(true);


        limitPrice = new DiscreteDistUniform(this, "LimitPriceStream", MIN_PRICE, MAX_PRICE, true, false);

        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        buyQueue = new Queue<BuyOrder>(this, "BuyQueue", true, true);
        sellQueue = new Queue<SellOrder>(this, "SellQueue", true, true);
    }

    /**
     * activate dynamic components
     */
    public void doInitialSchedules() {
        TradingAgentGeneratorEvent generator = new TradingAgentGeneratorEvent(this, "TradingAgentGenerator", true);

        generator.schedule(new TimeSpan(0, TimeUnit.SECONDS));

    }

    /**
     * returns a description of this model to be used in the report
     */
    public String description() {
        return "<Description of my model>";
    }

// define any additional methods if necessary,
// e.g. access methods to model components

    public double getAgentArrivalTime() {
        return agentArrivalTime.sample();
    }

    public int getLimitPrice() {
        return limitPrice.sample().intValue();
    }

    public boolean getBuyOrSell() {
        return buyOrSell.sample();
    }

    public <T extends Order> void submitOrder(T order) {
        if (order instanceof BuyOrder) {
            BuyOrder b = (BuyOrder) order;
            SellOrder s = sellQueue.first();

            if (s == null) {
                buyQueue.insert(b);
                return;
            }

            if (b.price >= s.price) {
                sellQueue.removeFirst();
                s.ta.traded(s.price);
                b.ta.traded(s.price);

            } else {
                buyQueue.insert(b);
            }
        } else {
            BuyOrder b = buyQueue.first();
            SellOrder s = (SellOrder) order;

            if (b == null) {
                sellQueue.insert(s);
                return;
            }

            if (s.price <= b.price) {
                buyQueue.removeFirst();
                b.ta.traded(b.price);
                s.ta.traded(b.price);

            } else {
                sellQueue.insert(s);
            }
        }

    }

    /**
     * runs the model
     */
    public static void main(String[] args) {

        // create model and experiment
        ExchangeModel model = new ExchangeModel();
        Experiment exp = new Experiment("Exp1");
        // and connect them
        model.connectToExperiment(exp);

        // set experiment parameters
        exp.setShowProgressBar(true);
        TimeInstant stopTime = new TimeInstant(1440, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);

        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }

} /* end of model class */