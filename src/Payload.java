public class Payload implements Comparable<Payload>{
    public TradingAgent agent;
    public MessageType type;
    public int price;

    @Override
    public int compareTo(Payload payload) {
        return price - payload.price;
    }
}
