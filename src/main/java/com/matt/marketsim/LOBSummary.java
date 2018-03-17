package com.matt.marketsim;

public class LOBSummary {
    public int depth;
    public IOrder[] buyOrders;
    public IOrder[] sellOrders;

    public LOBSummary(int depth) {
        this.depth = depth;
        buyOrders = new IOrder[depth];
        sellOrders = new IOrder[depth];
    }

    public IOrder getBestBuyOrder() {
        return buyOrders[0];
    }

    public IOrder getBestSellOrder() {
        return sellOrders[0];
    }
}
