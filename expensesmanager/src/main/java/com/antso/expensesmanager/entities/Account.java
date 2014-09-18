package com.antso.expensesmanager.entities;

import java.math.BigDecimal;

/**
 * This bean represents a financial account as a bank account, a wallet or a credit card
 */

public class Account {
    private String id;
    private String name;
    private BigDecimal initialBalance;
    private int color;

    public Account(final String id, final String name, final BigDecimal initialBalance,
                   final int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.initialBalance = initialBalance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
