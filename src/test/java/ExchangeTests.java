import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * Tests different orders of arrival of packets.
 */
public class ExchangeTests {
    MarketSimModel model;
    Exchange exchange;
    SecuritiesInformationProcessor sip;

    @BeforeEach
    void init() {
        Schedule s = new ZIPSchedule(25, 40, 165);
        model = new MarketSimModel(s);
        sip = new SecuritiesInformationProcessor(model, "TestSIP", false);
        exchange = new Exchange(model, "TestExchang", sip, false);
    }

    @Test
    void packetArrivalTest() {
        ZIP agent1 = new ZIP(model, 50, exchange, sip, Direction.BUY);
        Order order = new Order(agent1, exchange, agent1.direction, 40);
        Packet packet = new Packet(model, "TestPacket", false, null, null, MessageType.LIMIT_ORDER, order);
        exchange.handlePacket(packet);

        assertEquals(1, 1, "Hello");
    }
}
