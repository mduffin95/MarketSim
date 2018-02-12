

import desmoj.core.simulator.Model;

public class PriceUpdate extends Packet{


    public PriceUpdate(Model model, String name, boolean showInTrace, NetworkEntity source, NetworkEntity dest) {
        super(model, name, showInTrace, source, dest);
    }

    @Override
    public void arrived() {
        dest.handlePriceUpdate(this);
    }
}
