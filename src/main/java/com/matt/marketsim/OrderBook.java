package com.matt.marketsim;

import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {
    private PriorityQueue<Order> buyQueue;
    private PriorityQueue<Order> sellQueue;

    public OrderBook() {
        buyQueue = new PriorityQueue<>(10, Comparator.reverseOrder());
        sellQueue = new PriorityQueue<>(10);
    }

    public boolean remove(Order order) {
        if (order == null) {return false;}
        boolean b = buyQueue.remove(order);
        boolean s = sellQueue.remove(order);
        return (b || s);
    }

    public void add(Order order) {
        if (order == null) {
            return;
        }
        if (order.getDirection() == Direction.BUY) {
            buyQueue.add(order);
        } else {
            sellQueue.add(order);
        }
    }

    public Order getBestBuyOrder() {
        return buyQueue.peek();
    }

    public Order getBestSellOrder() {
        return sellQueue.peek();
    }

    public Order pollBestBuyOrder() {
        return buyQueue.poll();
    }

    public Order pollBestSellOrder() {
        return sellQueue.poll();
    }

    public void clear() {
        buyQueue.clear();
        sellQueue.clear();
    }

    public LOBSummary getSummary(SimClock clock) {
        return new LOBSummary(clock.getTime(), getBestBuyOrder(), getBestSellOrder());
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
