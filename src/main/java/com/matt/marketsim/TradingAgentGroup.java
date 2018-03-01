package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;

import java.util.ArrayList;

public class TradingAgentGroup {
    private ArrayList<TradingAgent> members = new ArrayList<>();
    private int equilibrium;

    public TradingAgentGroup(int equilibrium) {
        this.equilibrium = equilibrium;
    }

    public void addMember(TradingAgent ta) {
        members.add(ta);
    }

    public boolean contains(TradingAgent ta) {
        return members.contains(ta);
    }

    public int getTheoreticalUtility() {
        int theoreticalUtility = 0;
        for (TradingAgent ta: members) {
            int tmp = ta.getTheoreticalUtility(equilibrium);
            if (tmp > 0)
                theoreticalUtility += tmp;
        }
        return theoreticalUtility;
    }
}
