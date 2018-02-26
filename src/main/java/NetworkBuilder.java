import java.lang.reflect.Array;
import java.util.ArrayList;

public interface NetworkBuilder {

    void createNetworkEntities(MarketSimModel model, ArrayList<TradingAgent> tradingAgents, ArrayList<Exchange> exchanges, SecuritiesInformationProcessor sip);
}
