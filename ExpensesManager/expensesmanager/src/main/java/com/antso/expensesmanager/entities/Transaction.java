package com.antso.expensesmanager.entities;

import android.text.AndroidCharacter;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

/**
 * Created by asolano on 5/11/2014.
 *
 * This bean represent a financial transaction as:
 *  - money revenue
 *  - money transfer
 *  - money payment
 */
public class Transaction {
    private String id;

    private String description;
    private TransactionDirection direction;
    private TransactionType type;
    private String accountId;
    private String budgetId;

    private BigDecimal value;
    private DateTime dateTime;

    public Transaction(final String id, final String description,
            final TransactionDirection direction,
            final TransactionType type,
            final String accountId,
            final String budgetId,
            final BigDecimal value,
            final DateTime dateTime) {
        this.id = id;
        this.description = description;
        this.direction = direction;
        this.type = type;
        this.accountId = accountId;
        this.budgetId = budgetId;
        this.value = value;
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public TransactionType getType() {
        return type;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public DateTime getDateTime() {
        return dateTime;
    }



}
