package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import desmoj.core.simulator.TimeInstant;

public class LOBSummary {
    private QuoteData buyQuote;
    private QuoteData sellQuote;

    public LOBSummary() {
    }

    public LOBSummary(TimeInstant time, Exchange e,  Order buyQuote, Order sellQuote) {
        this.buyQuote = new QuoteData(time, e, buyQuote);
        this.sellQuote = new QuoteData(time, e, sellQuote);
    }

    public LOBSummary(QuoteData buyQuote, QuoteData sellQuote) {
        this.buyQuote = buyQuote;
        this.sellQuote = sellQuote;
    }

    public QuoteData getBuyQuote() {
        return buyQuote;
    }

    public QuoteData getSellQuote() {
        return sellQuote;
    }

    public void setBuyQuote(QuoteData quote) {
        buyQuote = quote;
    }

    public void setSellQuote(QuoteData quote) {
        sellQuote = quote;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!LOBSummary.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final LOBSummary other = (LOBSummary) obj;
        if ((this.buyQuote == null) ? (other.buyQuote != null) : !this.buyQuote.equals(other.buyQuote)) {
            return false;
        }
        if ((this.sellQuote == null) ? (other.sellQuote != null) : !this.sellQuote.equals(other.sellQuote)) {
            return false;
        }
        return true;
    }
}
