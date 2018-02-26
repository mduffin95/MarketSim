import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.security.krb5.internal.PAData;

import java.util.ArrayList;

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

    class DummyBuilder implements NetworkBuilder {

        @Override
        public void createNetworkEntities(MarketSimModel model, ArrayList<TradingAgent> tradingAgents, ArrayList<Exchange> exchanges, SecuritiesInformationProcessor sip) {
            return;
        }
    }


    @BeforeEach
    void init() {
        NetworkBuilder builder = new DummyBuilder();
        model = new MarketSimModel(builder);
        exp = new Experiment("Exp1");
        model.connectToExperiment(exp);

        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
        exchange = new Exchange(model, "TestExchange", sip, false);
    }

    @Test
    void packetArrivalTest() {
        ZIP agent1 = new ZIP(model, 50, exchange, sip, Direction.BUY);
        ZIP agent2 = new ZIP(model, 40, exchange, sip, Direction.SELL);

        Order orderBuy = new Order(agent1, exchange, agent1.direction, 40);
        Order orderSell = new Order(agent2, exchange, agent2.direction, 50);
        Packet packet1 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packet2 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packet1);
        assertEquals(orderBuy, exchange.getOrderBook().getBestBuyOrder(), "Buy order not inserted into order book correctly.");

        exchange.handlePacket(packet2);
        assertEquals(orderSell, exchange.getOrderBook().getBestSellOrder(), "Sell order not inserted into order book correctly");

    }

    @Test
    void repeatedPacketTest() {
        //When two packets arrive with the same order, it should only be inserted once.
    }

    @Test
    void cancelOrderTest() {
        //When an order is cancelled it should be removed from the order book
        ZIP agent1 = new ZIP(model, 50, exchange, sip, Direction.BUY);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, 40);
        Packet packet1 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderBuy);
        exchange.handlePacket(packet1);

        Packet cancelPacket = new Packet(model, "TestPacket", false, null, null, MessageType.CANCEL, orderBuy);
        exchange.handlePacket(cancelPacket);

        assertNull(exchange.getOrderBook().getBestBuyOrder());
    }

    @Test
    void orderMatchedTest() {
        //When an order arrives that matches with an order in the orderbook the trade should take place.

        int buyLimit = 50;
        int buyPrice = 45;
        int sellLimit = 30;
        int sellPrice = 35;

        ZIP agent1 = new ZIP(model, buyLimit, exchange, sip, Direction.BUY);
        ZIP agent2 = new ZIP(model, sellLimit, exchange, sip, Direction.SELL);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, buyPrice);
        Order orderSell = new Order(agent2, exchange, agent2.direction, sellPrice);
        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);

        Trade t = exchange.recentTrade;

        assertEquals(t.price, buyPrice);
        assertNull(exchange.getOrderBook().getBestBuyOrder());
        assertNull(exchange.getOrderBook().getBestSellOrder());
    }

    @Test
    void cancelOrderTooLateTest() {
        //If a cancel order arrives after the order that it is supposed to cancel is matched.
        //What happens if another limit order is sent behind the cancel?
        int buyLimit = 50;
        int buyPrice = 45;
        int sellLimit = 30;
        int sellPrice = 35;

        ZIP agent1 = new ZIP(model, buyLimit, exchange, sip, Direction.BUY);
        ZIP agent2 = new ZIP(model, sellLimit, exchange, sip, Direction.SELL);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, buyPrice);
        Order orderSell = new Order(agent2, exchange, agent2.direction, sellPrice);
        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);

        Packet packetCancel = new Packet(model, null, null, MessageType.CANCEL, orderSell);
        exchange.handlePacket(packetCancel);
    }
}
