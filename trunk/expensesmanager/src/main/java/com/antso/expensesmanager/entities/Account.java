package com.antso.expensesmanager.entities;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by asolano on 5/4/2014.
 *
 * This bean represents a financial account as a bank account, a wallet or a credit card
 */
public class Account {
    private String id;
    private String name;
    private BigDecimal initialBalance;
    private int color;

    //not on DB
    private BigDecimal balance;
    private BigDecimal monthIn;
    private BigDecimal monthOut;
    private BigDecimal monthBalance;

    private Collection<Transaction> transactions = null;

    public Account(final String id, final String name, final BigDecimal initialBalance,
                   final int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.initialBalance = initialBalance;
        this.balance = initialBalance;

        this.monthOut = BigDecimal.ZERO;
        this.monthIn = BigDecimal.ZERO;
        this.monthBalance = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setInitialBalance(final BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions = transactions;

        int currentMonth = DateTime.now().getMonthOfYear();
        int currentYear = DateTime.now().getYear();

        for (Transaction transaction : transactions) {
            if (transaction.getDirection().equals(TransactionDirection.Out)) {
                balance = balance.subtract(transaction.getValue());

                if(currentMonth == transaction.getDateTime().getMonthOfYear() &&
                    currentYear == transaction.getDateTime().getYear()) {
                    monthBalance = monthBalance.subtract(transaction.getValue());
                    monthOut = monthOut.add(transaction.getValue());
                }
            }
            if (transaction.getDirection().equals(TransactionDirection.In)) {
                balance = balance.add(transaction.getValue());

                if(currentMonth == transaction.getDateTime().getMonthOfYear() &&
                        currentYear == transaction.getDateTime().getYear()) {
                    monthBalance = monthBalance.add(transaction.getValue());
                    monthIn = monthOut.add(transaction.getValue());
                }
            }
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getMonthIn() {
        return monthIn;
    }

    public BigDecimal getMonthOut() {
        return monthOut;
    }

    public BigDecimal getMonthBalance() {
        return monthBalance;
    }

    public String getCurrency() {
        return "$";
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
