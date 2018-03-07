import com.matt.marketsim.Direction;
import com.matt.marketsim.FixedOrderRouter;
import com.matt.marketsim.MessageType;
import com.matt.marketsim.Order;
import com.matt.marketsim.FixedLimit;
import com.matt.marketsim.builders.NetworkBuilder;
import com.matt.marketsim.entities.agents.ZIP;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeSpan;
import com.matt.marketsim.entities.Exchange;
import com.matt.marketsim.entities.NetworkEntity;
import com.matt.marketsim.entities.Packet;
import com.matt.marketsim.entities.SecuritiesInformationProcessor;
import com.matt.marketsim.models.MarketSimModel;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    class DummyModel extends MarketSimModel {

        public DummyModel(NetworkBuilder builder) {
            super(builder);
        }

        @Override
        public TimeSpan getLatency(NetworkEntity a, NetworkEntity b) {
            return new TimeSpan(0);
        }
    }

    class DummyBuilder implements NetworkBuilder {

        @Override
        public SimpleWeightedGraph<NetworkEntity, DefaultWeightedEdge> createNetwork(MarketSimModel model) {
            return null;
        }
    }


    @BeforeEach
    void init() {
        NetworkBuilder builder = new DummyBuilder();
        model = new DummyModel(builder);
        exp = new Experiment("Exp1");
        model.connectToExperiment(exp);

        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
        exchange = new Exchange(model, "TestExchange", sip, false);
        clock = exp.getSimClock();
    }

    @Test
    void packetArrivalTest() {
        ZIP agent1 = new ZIP(model, new FixedLimit(50), new FixedOrderRouter(clock, exchange), Direction.BUY);
        ZIP agent2 = new ZIP(model, new FixedLimit(40), new FixedOrderRouter(clock, exchange), Direction.SELL);

        Order orderBuy = new Order(agent1, exchange, agent1.direction, 40, clock.getTime());
        Order orderSell = new Order(agent2, exchange, agent2.direction, 50, clock.getTime());
        Packet packet1 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packet2 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packet1);
        assertEquals(orderBuy, exchange.getOrderBook().getBestBuyOrder(), "Buy order not inserted into order book correctly.");

        exchange.handlePacket(packet2);
        assertEquals(orderSell, exchange.getOrderBook().getBestSellOrder(), "Sell order not inserted into order book correctly");

    }

    @Test
    void cancelOrderTest() {
        //When an order is cancelled it should be removed from the order book
        ZIP agent1 = new ZIP(model, new FixedLimit(50), new FixedOrderRouter(clock, exchange), Direction.BUY);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, 40, clock.getTime());
        Packet packet1 = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, orderBuy);
        exchange.handlePacket(packet1);

        Packet cancelPacket = new Packet(model, "TestPacket", false, null, null, MessageType.CANCEL, orderBuy);
        exchange.handlePacket(cancelPacket);

        assertNull(exchange.getOrderBook().getBestBuyOrder());
    }

    @Test
    void orderMatchedTest() {
        //When an order arrives that matches with an order in the order book the trade should take place. Also tests
        //that the trade takes place at the price of the order sitting on the order book.

        FixedLimit buyLimit = new FixedLimit(50);
        int buyPrice = 45;
        FixedLimit sellLimit = new FixedLimit(30);
        int sellPrice = 35;

        ZIP agent1 = new ZIP(model, buyLimit, new FixedOrderRouter(clock, exchange), Direction.BUY);
        ZIP agent2 = new ZIP(model, sellLimit, new FixedOrderRouter(clock, exchange), Direction.SELL);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, buyPrice, clock.getTime());
        Order orderSell = new Order(agent2, exchange, agent2.direction, sellPrice, clock.getTime());
        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);


        assertEquals(exchange.recentTrade.price, buyPrice);
        assertNull(exchange.getOrderBook().getBestBuyOrder());
        assertNull(exchange.getOrderBook().getBestSellOrder());

        exchange.handlePacket(packetSell);
        exchange.handlePacket(packetBuy);

        assertEquals(exchange.recentTrade.price, sellPrice);
        assertNull(exchange.getOrderBook().getBestBuyOrder());
        assertNull(exchange.getOrderBook().getBestSellOrder());

    }

    @Test
    void cancelOrderTooLateTest() {
        //If a cancel order arrives after the order that it is supposed to cancel is matched.
        //What happens if another limit order is sent behind the cancel?
        FixedLimit buyLimit = new FixedLimit(50);
        int buyPrice = 45;
        FixedLimit sellLimit = new FixedLimit(30);
        int sellPrice = 35;

        ZIP agent1 = new ZIP(model, buyLimit, new FixedOrderRouter(clock, exchange), Direction.BUY);
        ZIP agent2 = new ZIP(model, sellLimit, new FixedOrderRouter(clock, exchange), Direction.SELL);
        Order orderBuy = new Order(agent1, exchange, agent1.direction, buyPrice, clock.getTime());
        Order orderSell = new Order(agent2, exchange, agent2.direction, sellPrice, clock.getTime());
        Packet packetBuy = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderBuy);
        Packet packetSell = new Packet(model, null, null, MessageType.LIMIT_ORDER, orderSell);
        exchange.handlePacket(packetBuy);
        exchange.handlePacket(packetSell);

        Packet packetCancel = new Packet(model, null, null, MessageType.CANCEL, orderSell);
        exchange.handlePacket(packetCancel);
    }
}
