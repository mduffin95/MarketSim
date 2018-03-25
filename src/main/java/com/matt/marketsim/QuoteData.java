package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.experimentation.util.Run;

import java.util.Objects;

//An Order/Time pair used for quotes/updates
public class QuoteData {
    public Exchange exchange;
    public int price;
    public TimeInstant validTime;
//    private boolean empty;

    public QuoteData(Exchange exchange, int price, TimeInstant validTime) {
        this.exchange = exchange;
        this.price = price;
        this.validTime = validTime;
//        this.empty = false;
    }

    public QuoteData(TimeInstant validTime, Exchange e, Order order) {
        this.exchange = Objects.requireNonNull(e, "Exchange must not be null.");
        this.validTime = Objects.requireNonNull(validTime, "Time must not be null.");
        Objects.requireNonNull(order, "Order must not be null.");
        this.exchange = order.getExchange();
        this.price = order.getPrice();
    }

//    public boolean isEmpty() {
//        return empty;
//    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!QuoteData.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final QuoteData other = (QuoteData) obj;
        if (this.exchange == other.exchange && this.price == other.price) {
            return true;
        }
        return false;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public int getPrice() {
        return price;
    }

    public TimeInstant getValidTime() {
        return validTime;
    }

    public boolean moreRecentThan(QuoteData quote) {
        return TimeInstant.isAfterOrEqual(getValidTime(), quote.getValidTime());
    }

    public static boolean greaterThan(QuoteData a, QuoteData b) {
        if (null != a && null == b) {
            return true;
        }
        if (null == a) {
            return false;
        }
        return a.getPrice() > b.getPrice();
    }

    public static boolean lessThan(QuoteData a, QuoteData b) {
        if (null == a && null != b) {
            return true;
        }
        if (null == b) {
            return false;
        }
        return a.getPrice() < b.getPrice();
    }
}
