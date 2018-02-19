import desmoj.core.simulator.TimeInstant;

public class Order implements Comparable<Order> {
    public TradingAgent agent;
    private Exchange exchange;
    public Direction direction;
    private TimeInstant timeStamp;
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

    public void setTimeStamp(TimeInstant timeStamp) {
        this.timeStamp = timeStamp;
    }
}
