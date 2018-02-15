public class Order implements Comparable<Order> {
    public TradingAgent agent;
    public Exchange exchange;
    public Direction direction;
    public int price;

    public Order(TradingAgent agent, Exchange exchange, Direction direction, int price) {
        this.agent = agent;
        this.exchange = exchange;
        this.direction = direction;
        this.price = price;
    }

    @Override
    public int compareTo(Order payload) {
        return price - payload.price;
    }
}
