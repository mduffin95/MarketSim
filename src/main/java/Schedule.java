

public interface Schedule {

    //TODO: supply a list of TradingAgents instead of scheduling itself.
    void createAgents(MarketSimModel model, Exchange exchange, SecuritiesInformationProcessor sip);
}
