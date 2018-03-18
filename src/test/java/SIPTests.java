import com.matt.marketsim.*;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.entities.agents.TradingAgent;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.models.MarketSimModel;
import com.matt.marketsim.models.TwoMarketModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        model = new TwoMarketModel(15000, 250, 0,0,0,0,0,0,0,0);
        exp = new Experiment("Exp1");
        model.connectToExperiment(exp);

        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
        exchange1 = new Exchange(model, "Exchange1", sip, false);
        exchange2 = new Exchange(model, "Exchange2", sip, false);
        clock = exp.getSimClock();
        generator = new Random();


        agent1 = new ZIP(model, buyLimit, new FixedOrderRouter(clock, exchange1), Direction.BUY, generator, false);
        agent2 = new ZIP(model, sellLimit, new FixedOrderRouter(clock, exchange1), Direction.SELL, generator, false);
        buyOrder = new Order(agent1, exchange1, agent1.direction, buyPrice, buyLimit);
        sellOrder = new Order(agent2, exchange1, agent2.direction, sellPrice, sellLimit);
    }

    @Test
    void emptyNBBO_SingleNewBuyOrder() {
        LOBSummary lobSummary = new LOBSummary(1);
        lobSummary.buyOrders[0] = buyOrder;
        MarketUpdate marketUpdate = new MarketUpdate(exchange1,null, lobSummary);
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate);

        assertEquals(buyOrder, update.summary.getBestBuyOrder());
    }

    @Test
    void emptyNBBO_SingleNewSellOrder() {
        LOBSummary lobSummary = new LOBSummary(1);
        lobSummary.sellOrders[0] = sellOrder;
        MarketUpdate marketUpdate = new MarketUpdate(exchange1,null, lobSummary);
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate);

        assertEquals(sellOrder, update.summary.getBestSellOrder());
    }

    /*
     * Exchange 1 has a best bid price of 100, while exchange 2 has a best bid price of 95. A transaction occurs on
     * exchange 1 that causes the bid of 100 to transact. The next best on the orderbook is a bid of 90, which is
     * sent in a market update to the SIP. This should cause the SIP to use the best bid from exchange 2 (95) instead
     * of the best bid from exchange 1 (90).
     */
    @Test
    void newTransactionChangesBestBidExchange() {
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 100, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 95, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 90, 100);
        LOBSummary lobSummary1 = new LOBSummary(1);
        LOBSummary lobSummary2 = new LOBSummary(1);
        LOBSummary lobSummary3 = new LOBSummary(1);
        lobSummary1.buyOrders[0] = buyOrder1;
        lobSummary2.buyOrders[0] = buyOrder2;
        lobSummary3.buyOrders[0] = buyOrder3;
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3); //From exchange 1. Should lead to buyOrder2 being best.

        assertEquals(buyOrder2, update.summary.getBestBuyOrder());
    }

    @Test
    void betterBidArrivesAtExchange1() {
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 95, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 90, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 100, 100);
        LOBSummary lobSummary1 = new LOBSummary(1);
        LOBSummary lobSummary2 = new LOBSummary(1);
        LOBSummary lobSummary3 = new LOBSummary(1);
        lobSummary1.buyOrders[0] = buyOrder1;
        lobSummary2.buyOrders[0] = buyOrder2;
        lobSummary3.buyOrders[0] = buyOrder3;
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3); //From exchange 1. Should lead to buyOrder2 being best.

        assertEquals(buyOrder3, update.summary.getBestBuyOrder());
    }

    @Test
    void bestBidDecreases() {
        Order buyOrder1 = new Order(agent1, exchange1, agent1.direction, 100, 100);
        Order buyOrder2 = new Order(agent1, exchange2, agent1.direction, 90, 100);
        Order buyOrder3 = new Order(agent2, exchange1, agent1.direction, 95, 100);
        LOBSummary lobSummary1 = new LOBSummary(1);
        LOBSummary lobSummary2 = new LOBSummary(1);
        LOBSummary lobSummary3 = new LOBSummary(1);
        lobSummary1.buyOrders[0] = buyOrder1;
        lobSummary2.buyOrders[0] = buyOrder2;
        lobSummary3.buyOrders[0] = buyOrder3;
        MarketUpdate marketUpdate1 = new MarketUpdate(exchange1,null, lobSummary1);
        MarketUpdate marketUpdate2 = new MarketUpdate(exchange2,null, lobSummary2);
        MarketUpdate marketUpdate3 = new MarketUpdate(exchange1,null, lobSummary3);
        sip.marketUpdateHelper(marketUpdate1); //From exchange 1
        sip.marketUpdateHelper(marketUpdate2); //From exchange 2
        MarketUpdate update = sip.marketUpdateHelper(marketUpdate3); //From exchange 1. Should lead to buyOrder2 being best.

        assertEquals(buyOrder3, update.summary.getBestBuyOrder());
    }
}
