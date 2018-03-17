package com.matt.marketsim;

public class LOBSummary {
    public int depth;
    public IOrder[] buyOrders;
    public IOrder[] sellOrders;

    public LOBSummary(int depth) {
        this.depth = depth;
        buyOrders = new Order[depth];
        sellOrders = new Order[depth];
    }

    public IOrder getBestBuyOrder() {
        return buyOrders[0];
    }

    public IOrder getBestSellOrder() {
        return sellOrders[0];
    }
}
