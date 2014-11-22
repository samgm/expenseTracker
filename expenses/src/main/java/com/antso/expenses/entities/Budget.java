package com.antso.expenses.entities;

import com.antso.expenses.enums.TimeUnit;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * This bean represents an expenses budget for a given amount of time
 */

public class Budget {

    private String id;
    private String name;
    private BigDecimal threshold;
    private int color;

    private TimeUnit periodUnit;
    private int periodLength;
    private DateTime periodStart;

    public Budget(final String id, final String name, final BigDecimal threshold,
                   final int color, final int periodLength, final TimeUnit periodUnit,
                   final DateTime periodStart) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.threshold = threshold;
        this.periodLength = periodLength;
        this.periodUnit = periodUnit;
        this.periodStart = periodStart;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public int getColor() {
        return color;
    }

    public TimeUnit getPeriodUnit() {
        return periodUnit;
    }

    public int getPeriodLength() {
        return periodLength;
    }

    public DateTime getPeriodStart() {
        return periodStart;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;

        Budget that = (Budget) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
