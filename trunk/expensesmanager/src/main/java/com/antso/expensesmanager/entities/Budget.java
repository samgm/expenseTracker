package com.antso.expensesmanager.entities;

import com.antso.expensesmanager.enums.BudgetPeriodUnit;

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

    private BudgetPeriodUnit periodUnit;
    private int periodLength;
    private DateTime periodStart;

    public Budget(final String id, final String name, final BigDecimal threshold,
                   final int color, final int periodLength, final BudgetPeriodUnit periodUnit,
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

    public BudgetPeriodUnit getPeriodUnit() {
        return periodUnit;
    }

    public int getPeriodLength() {
        return periodLength;
    }

    @Override
    public String toString() {
        return name;
    }

    public DateTime getPeriodStart() {
        return periodStart;
    }
}
