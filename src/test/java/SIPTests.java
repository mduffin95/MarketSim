import com.matt.marketsim.*;
import com.matt.marketsim.entities.CDA;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.models.DummyModel;
import com.matt.marketsim.models.MarketSimModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*
 * Tests different orders of arrival of packets.
 */
public class SIPTests {
    MarketSimModel model;
    Exchange exchange1;
    Exchange exchange2;
    SecuritiesInformationProcessor sip;
    Experiment exp;
    SimClock clock;
    Random generator;

    int buyLimit = 50;
    int buyPrice = 45;
    int sellLimit = 30;
    int sellPrice = 35;

    ZIP agent1;
    ZIP agent2;
    Order buyOrder;
    Order sellOrder;

    @BeforeEach
    void init() {
        model = new DummyModel();
        exp = new Experiment("Exp1");
        model.connectToExperiment(exp);

        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
        exchange1 = new CDA(model, "Exchange1", sip, false);
        exchange2 = new CDA(model, "Exchange2", sip, false);
        clock = exp.getSimClock();
        generator = new Random();


        agent1 = new ZIP(model, buyLimit, new FixedOrderRouter(clock, exchange1), Direction.BUY, generator, false);
        agent2 = new ZIP(model, sellLimit, new FixedOrderRouter(clock, exchange1), Direction.SELL, generator, false);
        buyOrder = new Order(agent1, exchange1, agent1.direction, buyPrice, buyLimit);
        sellOrder = new Order(agent2, exchange1, agent2.direction, sellPrice, sellLimit);
    }

    @Test
    void emptyNBBO_SingleNewBuyOrder() {
        LOBSummary lobSummary = new LOBSummary(new TimeInstant(0), buyOrder, null);
        MarketUpdate marketUpdate = new MarketUpdate(exchange1,null, lobSummary);
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate).get();

        assertEquals(buyOrder.getTimeStampedOrder(new TimeInstant(0)), update.getSummary().getBuyOrder());
    }

    @Test
    void emptyNBBO_SingleNewSellOrder() {
        LOBSummary lobSummary = new LOBSummary(new TimeInstant(0), null, sellOrder);
        MarketUpdate marketUpdate = new MarketUpdate(exchange1,null, lobSummary);
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate).get();

        assertEquals(sellOrder.getTimeStampedOrder(new TimeInstant(0)), update.getSummary().getSellOrder());
    }

    /*
     * Exchange 1 has a best bid price of 100, while exchange 2 has a best bid price of 95. A transaction occurs on
     * exchange 1 that causes the bid of 100 to transact. The next best on the orderbook is a bid of 90, which is
     * sent in a market update to the SIP. This should cause the SIP to use the best bid from exchange 2 (95) instead
     * of the best bid from exchange 1 (90).
     */
    @Test
    void newTransactionChangesBestBidExchange() {
        //After the first buy order, exchange 1 has the best bid. After the 2nd it's still exchange 1. However, after
        //the 3rd buy order it switches to exchange 2 because the 3rd 'overwrites' the 1st, effectively notifying that
        //a trade has occurred.
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 100, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 95, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 90, 100);
        LOBSummary lobSummary1 = new LOBSummary(new TimeInstant(0), buyOrder1, null);
        LOBSummary lobSummary2 = new LOBSummary(new TimeInstant(1), buyOrder2, null);
        LOBSummary lobSummary3 = new LOBSummary(new TimeInstant(2), buyOrder3, null);
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3).get(); //From exchange 1. Should lead to buyOrder2 being best.

        assertEquals(buyOrder2.getTimeStampedOrder(new TimeInstant(1)), update.getSummary().getBuyOrder());
    }

    @Test
    void betterBidArrivesAtExchange1() {
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 95, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 90, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 100, 100);
        LOBSummary lobSummary1 = new LOBSummary(new TimeInstant(0), buyOrder1, null);
        LOBSummary lobSummary2 = new LOBSummary(new TimeInstant(1), buyOrder2, null);
        LOBSummary lobSummary3 = new LOBSummary(new TimeInstant(2), buyOrder3, null);
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3).get(); //From exchange 1. Should lead to buyOrder3 being best.

        assertEquals(buyOrder3.getTimeStampedOrder(new TimeInstant(2)), update.getSummary().getBuyOrder());
    }

    @Test
    void bestBidDecreases() {
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 100, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 90, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 95, 100);
        LOBSummary lobSummary1 = new LOBSummary(new TimeInstant(0), buyOrder1, null);
        LOBSummary lobSummary2 = new LOBSummary(new TimeInstant(0), buyOrder2, null);
        LOBSummary lobSummary3 = new LOBSummary(new TimeInstant(0), buyOrder3, null);
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3).get(); //From exchange 1. Should lead to buyOrder2 being best.

        assertEquals(buyOrder3.getTimeStampedOrder(new TimeInstant(0)), update.getSummary().getBuyOrder());
    }
}
