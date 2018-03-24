import com.matt.marketsim.*;
import com.matt.marketsim.entities.*;
import com.matt.marketsim.entities.agents.ZIP;
import com.matt.marketsim.models.DummyModel;
import com.matt.marketsim.models.TwoMarketModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimClock;
import com.matt.marketsim.models.MarketSimModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*
 * Tests different orders of arrival of packets.
 */
public class ExchangeTests {
    MarketSimModel model;
    Exchange exchange;
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
        exp.setReferenceUnit(TimeUnit.MILLISECONDS);
        model.connectToExperiment(exp);

//        sip = new SecuritiesInformationProcessor(null, "TestSIP", false);
        exchange = new Exchange(model, "Exchange", sip, false);
        clock = exp.getSimClock();
        generator = new Random();



        agent1 = new ZIP(model, buyLimit, new FixedOrderRouter(clock, exchange), Direction.BUY, generator, false);
        agent2 = new ZIP(model, sellLimit, new FixedOrderRouter(clock, exchange), Direction.SELL, generator, false);
        buyOrder = new Order(agent1, exchange, agent1.direction, buyPrice, buyLimit);
        sellOrder = new Order(agent2, exchange, agent2.direction, sellPrice, sellLimit);
    }

//    @Test
//    void packetArrivalTest() {
//
//        Order orderBuy = new DummyOrder(40, 50, Direction.BUY);
//        Order orderSell = new DummyOrder(50, 40, Direction.SELL);
//        exchange.onLimitOrder(orderBuy);
//        assertEquals(orderBuy, exchange.getOrderBook().getBuyQuote(), "Buy order not inserted into order book correctly.");
//
//        exchange.onLimitOrder(orderSell);
//        assertEquals(orderSell, exchange.getOrderBook().getBestSellOrder(), "Sell order not inserted into order book correctly");
//
//    }

    @Test
    void cancelOrderTest() {
        exchange.onLimitOrder(buyOrder);

        exchange.onCancelOrder(buyOrder);

        assertNull(exchange.getOrderBook().getBestBuyOrder());
    }

    @Test
    void orderMatchedTest() {
        //When an order arrives that matches with an order in the order book the trade should take place. Also tests
        //that the trade takes place at the price of the order sitting on the order book.

        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, buyOrder);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, sellOrder);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);


        assertEquals(exchange.recentTrade.getPrice(), buyPrice);
        assertNull(exchange.getOrderBook().getBestBuyOrder());
        assertNull(exchange.getOrderBook().getBestSellOrder());

        exchange.handlePacket(packetSell);
        exchange.handlePacket(packetBuy);

        assertEquals(exchange.recentTrade.getPrice(), sellPrice);
        assertNull(exchange.getOrderBook().getBestBuyOrder());
        assertNull(exchange.getOrderBook().getBestSellOrder());

    }

    @Test
    void cancelOrderTooLateTest() {
        //If a cancel order arrives after the order that it is supposed to cancel is matched.
        //What happens if another limit order is sent behind the cancel?

        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, buyOrder);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, sellOrder);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);

        Packet packetCancel = new Packet(model, null, null, MessageType.CANCEL, sellOrder);
        exchange.handlePacket(packetCancel);
    }
}
