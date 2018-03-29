package com.matt.marketsim;
import com.google.gson.Gson;

public class MarketParams {
    public final double SIGMA_SHOCK;
    public final double SIGMA_PV;
    public final double k;
    public final double MEAN_FUNDAMENTAL;
    public final double ALPHA; //Arbitrageur threshold
    public final double OFFSET_RANGE;
    public final double LAMBDA;
    public final double DISCOUNT_RATE;
    public final double DELTA;
    public final int SIM_LENGTH;
    public final int NUM_EXCHANGES; //How much to increment delta by each time
    public final int AGENTS_PER_EXCHANGE; //Make sure this is even

    public MarketParams(int simLength, int num_exchanges, int agents_per_exchange, double alpha, double mean_fundamental, double k, double var_pv, double var_shock, double range, double lambda, double discountRate, double delta) {
        this.SIM_LENGTH = simLength;
        this.ALPHA = alpha;
        this.MEAN_FUNDAMENTAL = mean_fundamental;
        this.k = k;
        this.SIGMA_PV = Math.sqrt(var_pv);
        this.SIGMA_SHOCK = Math.sqrt(var_shock);
        this.OFFSET_RANGE = range;
        this.LAMBDA = lambda;
        this.DELTA = delta;
        this.AGENTS_PER_EXCHANGE = agents_per_exchange;
        this.NUM_EXCHANGES = num_exchanges;
        this.DISCOUNT_RATE = discountRate;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
