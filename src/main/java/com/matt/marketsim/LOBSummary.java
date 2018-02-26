package com.matt.marketsim;

public class LOBSummary {
    public int depth;
    public Order[] buyOrders;
    public Order[] sellOrders;

    public LOBSummary(int depth) {
        this.depth = depth;
        buyOrders = new Order[depth];
        sellOrders = new Order[depth];
    }

    public Order getBestBuyOrder() {
        return buyOrders[0];
    }

    public Order getBestSellOrder() {
        return sellOrders[0];
    }
}
