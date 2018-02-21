import desmoj.core.simulator.TimeInstant;

public class Trade {
    public TimeInstant time;
    public Exchange exchange;
    public int price;
    public int quantity;
    public TradingAgent buyer;
    public TradingAgent seller;


    public Trade(TimeInstant time, int price, int quantity, TradingAgent buyer, TradingAgent seller) {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.buyer = buyer;
        this.seller = seller;
    }
}