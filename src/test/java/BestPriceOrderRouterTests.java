import com.matt.marketsim.*;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.entities.agents.ZIU;
import com.matt.marketsim.models.DummyModel;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.models.TwoMarketModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*
 * Tests different orders of arrival of packets.
 */
public class BestPriceOrderRouterTests {

    TradingAgent tradingAgent;
    BestPriceOrderRouter orderRouter;
    Exchange exchange;

    @BeforeEach
    void init() {
        Model model = new DummyModel();
//        tradingAgent = new ZIU(null, );
        SimClock clock = new SimClock("Clock");
        exchange = new Exchange(null, "Exchange", null, false);
        orderRouter = new BestPriceOrderRouter(clock, exchange);
    }


    @Test
    void orderRouterTest() {
        MarketUpdate update = new MarketUpdate(exchange, null, null);

        orderRouter.respond(update);

    }
}
