import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.ArrayList;
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

    public void remove(Order order) {
        if (order == null) {return;}
        buyQueue.remove(order);
        sellQueue.remove(order);
    }

    public void add(Order order) {
        if (order == null) {
            return;
        }
        if (order.direction == Direction.BUY) {
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

    //TODO: Make this more efficient
    public PriceQuote getPriceQuote(int depth) {
        Order[] buyArray = buyQueue.toArray(new Order[buyQueue.size()]);
        Order[] sellArray = sellQueue.toArray(new Order[sellQueue.size()]);

        Arrays.sort(buyArray, Comparator.reverseOrder());
        Arrays.sort(sellArray);

        PriceQuote priceQuote = new PriceQuote(depth);
        for(int i=0; i<depth; i++) {
            priceQuote.buyOrders[i] = i<buyArray.length ? buyArray[i] : null;
            priceQuote.sellOrders[i] = i<sellArray.length ? sellArray[i] : null;
        }

        return priceQuote;
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
