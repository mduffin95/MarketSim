package com.matt.marketsim;

import com.matt.marketsim.entities.Exchange;
import desmoj.core.simulator.TimeInstant;

//An Order/Time pair used for quotes/updates
public class QuoteData {
    public Exchange exchange;
    public Direction direction;
    public int price;
    public TimeInstant validTime;

    public QuoteData(Exchange exchange, Direction direction, int price, TimeInstant validTime) {
        this.exchange = exchange;
        this.direction = direction;
        this.price = price;
        this.validTime = validTime;
    }

    public QuoteData(TimeInstant validTime, Order order) {
        if (null != order) {
            this.exchange = order.getExchange();
            this.direction = order.getDirection();
            this.price = order.getPrice();
        }
        this.validTime = validTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!QuoteData.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final QuoteData other = (QuoteData) obj;
        if (this.exchange == other.exchange && this.direction == other.direction && this.price == other.price) {
            return true;
        }
        return false;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public Direction getDirection() {
        return direction;
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
