package com.matt.marketsim.builders;

import static java.lang.Math.min;

public class Schedule {
    int lowestBuy;
    int buyStep;
    int buyQuantity;
    int[] buySchedule;

    int lowestSell;
    int sellStep;
    int sellQuantity;
    int[] sellSchedule;



    public Schedule(int lowestBuy, int buyStep, int buyQuantity, int lowestSell, int sellStep, int sellQuantity) {
        this.buyQuantity = buyQuantity;
        this.sellQuantity = sellQuantity;
        buySchedule = new int[buyQuantity];
        sellSchedule = new int[sellQuantity];
        for (int i=0; i<buyQuantity; i++) {
            int j = buyQuantity-i-1;
            buySchedule[i] = lowestBuy + j*buyStep;
        }

        for (int i=0; i<sellQuantity; i++) {
            sellSchedule[i] = lowestSell + i*sellStep;
        }
    }

    public int[] getBuySchedule() {
        return buySchedule;
    }

    public int[] getSellSchedule() {
        return sellSchedule;
    }

    public int getEquilibriumPrice() { //TODO: Not sure this is totally correct. What if the buyQuantity and sellQuantity are different?
        int minQuantity = min(buyQuantity, sellQuantity);

        for (int i=0; i<minQuantity; i++) {
            int b = buySchedule[i];
            int s = sellSchedule[i];
            if (b > s) {
                return (b+s) / 2;
            }
        }
        return -1;
    }

    public int getTheoreticalUtility() {
        int equilibrium = getEquilibriumPrice();
        int theoreticalMaxUtility = 0;
        int utility;

        for (int i=0; i<buyQuantity; i++) {
            int j = buyQuantity-i-1;
            utility = buySchedule[i] - equilibrium;
            if (utility > 0) theoreticalMaxUtility += utility;
        }

        for (int i=0; i<sellQuantity; i++) {
            utility = equilibrium - sellSchedule[i];
            if (utility > 0) theoreticalMaxUtility += utility;
        }
        return theoreticalMaxUtility;
    }
//
//    public int getEquilibriumQuantity() {
//
//    }
}
