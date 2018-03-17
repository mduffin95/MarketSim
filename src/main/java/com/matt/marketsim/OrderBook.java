package com.matt.marketsim;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {
    private PriorityQueue<IOrder> buyQueue;
    private PriorityQueue<IOrder> sellQueue;

    public OrderBook() {
        buyQueue = new PriorityQueue<>(10, Comparator.reverseOrder());
        sellQueue = new PriorityQueue<>(10);
    }

    public boolean remove(IOrder order) {
        if (order == null) {return false;}
        boolean b = buyQueue.remove(order);
        boolean s = sellQueue.remove(order);
        return (b || s);
    }

    public void add(IOrder order) {
        if (order == null) {
            return;
        }
        if (order.getDirection() == Direction.BUY) {
            buyQueue.add(order);
        } else {
            sellQueue.add(order);
        }
    }

    public IOrder getBestBuyOrder() {
        return buyQueue.peek();
    }

    public IOrder getBestSellOrder() {
        return sellQueue.peek();
    }

    public IOrder pollBestBuyOrder() {
        return buyQueue.poll();
    }

    public IOrder pollBestSellOrder() {
        return sellQueue.poll();
    }

    public void clear() {
        buyQueue.clear();
        sellQueue.clear();
    }

    //TODO: Make this more efficient
    public LOBSummary getSummary(int depth) {
        IOrder[] buyArray = buyQueue.toArray(new IOrder[buyQueue.size()]);
        IOrder[] sellArray = sellQueue.toArray(new IOrder[sellQueue.size()]);

        Arrays.sort(buyArray, Comparator.reverseOrder());
        Arrays.sort(sellArray);

        LOBSummary summary = new LOBSummary(depth);
        for(int i=0; i<depth; i++) {
            summary.buyOrders[i] = i<buyArray.length ? buyArray[i] : null;
            summary.sellOrders[i] = i<sellArray.length ? sellArray[i] : null;
        }

        return summary;
    }

    public void printOrderBook() {
        System.out.println("Buy Queue");
        while(!buyQueue.isEmpty()){
            System.out.println(buyQueue.poll().getPrice());
        }
        System.out.println("Sell Queue");
        while(!sellQueue.isEmpty()){
            System.out.println(sellQueue.poll().getPrice());
        }
    }
}
