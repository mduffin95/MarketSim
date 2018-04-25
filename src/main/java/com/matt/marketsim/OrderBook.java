package com.matt.marketsim;

import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;

import javax.swing.text.html.Option;
import java.util.*;

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

    public Optional<Integer> findIntersectionPrice() {
        Order[] buyOrders = buyQueue.toArray(new Order[0]);
        Order[] sellOrders = sellQueue.toArray(new Order[0]);

        Arrays.sort(buyOrders, Collections.reverseOrder());
        Arrays.sort(sellOrders);

        Integer result = null;
        for (int i=0; i<buyQueue.size() && i<sellQueue.size(); i++) {
            Order buy = buyOrders[i];
            Order sell = sellOrders[i];

            if (buy.compareTo(sell) < 0)
                break;

            result = (int)Math.round((buy.getPrice() + sell.getPrice()) / 2.0);
        }
        return Optional.ofNullable(result);
    }

    public boolean canTrade() {
        Order b = getBestBuyOrder();
        Order s = getBestSellOrder();

        if (null == b || null == s)
            return false;
        if (b.compareTo(s) < 0)
            return false;
        return true;
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
