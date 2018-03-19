package com.matt.marketsim;

import com.matt.marketsim.entities.agents.TradingAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TradingAgentGroup {
    private Set<TradingAgent> members = new HashSet<>();
//    private int equilibrium;
//    private boolean equilibriumSet = false;

    public TradingAgentGroup() {

    }

    public TradingAgentGroup(int equilibrium) {
//        this.equilibrium = equilibrium;
//        equilibriumSet = true;
    }

    public void addMember(TradingAgent ta) {
        members.add(ta);
    }

    public boolean contains(TradingAgent ta) {
        return members.contains(ta);
    }

//    public int getTheoreticalUtility() {
//        if (!equilibriumSet) {
//            throw new UnsupportedOperationException("Equilibrium price not set.");
//        }
//        int theoreticalUtility = 0;
//        for (TradingAgent ta: members) {
//            int tmp = ta.getTheoreticalUtility(equilibrium);
//            if (tmp > 0)
//                theoreticalUtility += tmp;
//        }
//        return theoreticalUtility;
//    }
}
