

public class MarketUpdate {
    public Trade trade;
    public LOBSummary summary;

    public MarketUpdate(Trade trade, LOBSummary summary) {
        this.trade = trade;
        this.summary = summary;
    }
}
