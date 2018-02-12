package models;

import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.simulator.*;
import entities.Exchange;
import entities.TradingAgent;
import events.TradingAgentGeneratorEvent;

import java.util.concurrent.TimeUnit;

public class MarketSimModel extends Model {


    // define model components here
    //Random number stream used to draw an arrival time for the next trading agent
    private ContDistExponential agentArrivalTime;

    //Random number stream used to draw a limit price for the next trading agent
    private DiscreteDistUniform limitPrice;

    //Random boolean used to determine whether agent is buying or selling
    private BoolDistBernoulli buyOrSell;

    public static int MIN_PRICE = 10;
    public static int MAX_PRICE = 1000;
    public static int NUM_TRADERS = 10;

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
        TradingAgentGeneratorEvent generator = new TradingAgentGeneratorEvent(this, "TradingAgentGenerator", true);

        generator.schedule(new TimeSpan(0, TimeUnit.SECONDS));
    }

    @Override
    public void init() {
        agentArrivalTime = new ContDistExponential(this, "AgentArrivalTimeStream", 3, true, false);
        agentArrivalTime.setNonNegative(true);

        limitPrice = new DiscreteDistUniform(this, "LimitPriceStream", MIN_PRICE, MAX_PRICE, true, false);

        buyOrSell = new BoolDistBernoulli(this, "BuyOrSell", 0.5, true, false);

        exchange = new Exchange(this, "Exchange", true);

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
        TimeInstant stopTime = new TimeInstant(1440, TimeUnit.SECONDS);
        exp.tracePeriod(new TimeInstant(0), stopTime);
        exp.stop(stopTime);

        // start experiment
        exp.start();

        // generate report and shut everything off
        exp.report();
        exp.finish();
    }

}
