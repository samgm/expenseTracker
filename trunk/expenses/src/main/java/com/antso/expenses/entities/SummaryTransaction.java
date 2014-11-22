package com.antso.expenses.entities;

import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class SummaryTransaction extends Transaction {
    final BigDecimal valueIn;
    final BigDecimal valueOut;
    final BigDecimal valueDiff;
    final BigDecimal balance;

    public BigDecimal getValueIn() {
        return valueIn;
    }

    public BigDecimal getValueOut() {
        return valueOut;
    }

    public BigDecimal getValueDiff() {
        return valueDiff;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public SummaryTransaction(BigDecimal valueIn,
                              BigDecimal valueOut,
                              BigDecimal valueDiff,
                              BigDecimal balance,
                              DateTime date) {
        super("", "", TransactionDirection.Undef, TransactionType.Summary, "", "", BigDecimal.ZERO, date);
        this.valueIn = valueIn;
        this.valueOut = valueOut;
        this.valueDiff = valueDiff;
        this.balance = balance;
    }
}
