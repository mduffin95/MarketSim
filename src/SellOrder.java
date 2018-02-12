import desmoj.core.simulator.*;

public class SellOrder extends Order {
    ExchangeModel exchangeModel;

    public SellOrder(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        exchangeModel = (ExchangeModel)owner;
    }

    @Override
    public int getQueueingPriority() {
        return exchangeModel.MAX_PRICE - price;
    }
}
