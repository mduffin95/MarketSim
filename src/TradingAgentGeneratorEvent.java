import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.*;

import java.util.concurrent.TimeUnit;

public class TradingAgentGeneratorEvent extends ExternalEvent {
    public TradingAgentGeneratorEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

    }

    @Override
    public void eventRoutine() {
        ExchangeModel model = (ExchangeModel)getModel();

        TradingAgent agent = new TradingAgent(model);

        ArrivalEvent agentArrival = new ArrivalEvent(model, "AgentArrivalEvent", true);

        agentArrival.schedule(agent, new TimeSpan(0, TimeUnit.SECONDS));

        //Schedule generator again
        schedule(new TimeSpan(model.getAgentArrivalTime(), TimeUnit.SECONDS));

    }
}
