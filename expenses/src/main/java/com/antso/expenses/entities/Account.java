package com.antso.expenses.entities;

import android.content.Context;

import com.antso.expenses.utils.Utils;

import java.math.BigDecimal;

/**
 * This bean represents a financial account as a bank account, a wallet or a credit card
 */

public class Account {
    private String id;
    private String name;
    private BigDecimal initialBalance;
    private int color;
    private boolean archived;

    public Account(final String id, final String name, final BigDecimal initialBalance,
                   final int color, final boolean archived) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.initialBalance = initialBalance;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;

        Account that = (Account) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
