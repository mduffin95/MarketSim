import com.matt.marketsim.*;
import com.matt.marketsim.entities.CDA;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.models.DummyModel;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * Tests different orders of arrival of packets.
 */
public class BestPriceOrderRouterTests {

    TradingAgent agent1;
    TradingAgent agent2;
    BestPriceOrderRouter orderRouter;
    Exchange exchange1;
    Exchange exchange2;
    SecuritiesInformationProcessor sip;
    Random generator;

    @BeforeEach
    void init() {
        MarketSimModel model = new DummyModel();
        Experiment exp = new Experiment("TestExperiment");
        model.connectToExperiment(exp);
//        tradingAgent = new ZIU(null, );
        exchange1 = new CDA(model, "Exchange", null, false);
        exchange2 = new CDA(model, "Exchange", null, false);
        generator = new Random();
        SimClock clock = new SimClock("clock");
        agent1 = new ZIP(model, 0, new FixedOrderRouter(clock, exchange1), Direction.SELL, generator, false);
        agent2 = new ZIP(model, 0, new FixedOrderRouter(clock, exchange1), Direction.SELL, generator, false);
        Set<Exchange> allExchanges = new HashSet<>();
        allExchanges.add(exchange1);
        allExchanges.add(exchange2);
        orderRouter = new BestPriceOrderRouter(null, exchange1, allExchanges);

        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
    }

    @Test
    void sendToPrimaryWhenEmpty() {
        Exchange e = orderRouter.findBestExchange(Direction.BUY, 105);

        assertEquals(exchange1, e);
    }

    @Test
    void orderRouterTest() {
        //Making sure delayed update from SIP doesn't remove legitimate summary

        Order o1 = new Order(agent1, exchange1, Direction.SELL, 105, 95);
        LOBSummary summary1 = new LOBSummary(new TimeInstant(0), null, o1);
        MarketUpdate update1 = new MarketUpdate(exchange1, null, summary1);
        orderRouter.respond(update1);


        Order o2 = new Order(agent2, exchange1, Direction.SELL, 100, 95);
        LOBSummary summary2 = new LOBSummary(new TimeInstant(1), null, o2);
        MarketUpdate update2 = new MarketUpdate(exchange1, null, summary2);
        orderRouter.respond(update2);

        LOBSummary summary3 = new LOBSummary(new TimeInstant(0), null, o1);
        MarketUpdate update3 = new MarketUpdate(sip, null, summary3);
        orderRouter.respond(update3);

//        LOBSummary summary4 = new LOBSummary();
//        summary4.sellQuote = new Order(null, exchange2, Direction.SELL, 104, 95);
//        MarketUpdate update4 = new MarketUpdate(sip, null, summary4);
//        orderRouter.respond(update4);

        OrderTimeStamped result = orderRouter.multiMarketView.getBestOffer().get();

        assertEquals(o2.getTimeStampedOrder(new TimeInstant(1)), result);

        result = orderRouter.multiMarketView.getBestOffer(exchange1).get();

        assertEquals(o2.getTimeStampedOrder(new TimeInstant(1)), result);

    }

    @Test
    void orderRouterTest2() {
        //Two offers are sent to an exchange. The better priced one then trades so we send a new update. Then the sip
        //sends an update with the old traded order due to delay.

        Order o1 = new Order(agent1, exchange1, Direction.SELL, 105, 95);
        LOBSummary summary1 = new LOBSummary(new TimeInstant(0), null, o1);
        MarketUpdate update1 = new MarketUpdate(exchange1, null, summary1);
        orderRouter.respond(update1);

        Order o2 = new Order(agent2, exchange1, Direction.SELL, 100, 95);
        LOBSummary summary2 = new LOBSummary(new TimeInstant(1), null, o2);
        MarketUpdate update2 = new MarketUpdate(exchange1, null, summary2);
        orderRouter.respond(update2);

        //o2 trades so we send this update
        LOBSummary summary3 = new LOBSummary(new TimeInstant(2), null, o1);
        Trade t = new Trade(new TimeInstant(2), 100, 1, new Order(null, exchange1, Direction.BUY, 101, 102), o2);
        MarketUpdate update3 = new MarketUpdate(exchange1, t, summary3);
        orderRouter.respond(update3);

        //This arrives later from the SIP
        LOBSummary summary4 = new LOBSummary(new TimeInstant(1), null, o2);
        MarketUpdate update4 = new MarketUpdate(sip, null, summary4);
        orderRouter.respond(update4);

//        Exchange e = orderRouter.findBestExchange(MessageType.LIMIT_ORDER, Direction.BUY, 105);
        OrderTimeStamped result = orderRouter.multiMarketView.getBestOffer().get();

        OrderTimeStamped q = o1.getTimeStampedOrder(new TimeInstant(2));

        assertEquals(q, result);

    }
}
