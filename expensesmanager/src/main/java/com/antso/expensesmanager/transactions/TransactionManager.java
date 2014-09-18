package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.util.Log;

import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.BudgetPeriodUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TransactionManager {
        TRANSACTION_MANAGER;

    private DatabaseHelper dbHelper = null;

    private TransactionManager() {
    }

    public void start(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }
    }


    public void stop() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void removeTransaction(Transaction transaction) {
        if(transaction.getLinkedTransactionId() != null &&
                !transaction.getLinkedTransactionId().isEmpty()) {
            dbHelper.deleteTransaction(transaction.getLinkedTransactionId());
        }
        dbHelper.deleteTransaction(transaction.getId());
    }

    public Collection<Transaction> getTransactions(TransactionDirection direction, boolean noTransfer) {
        return dbHelper.getTransactions(direction, noTransfer);
    }

    public Collection<Transaction> getTransactions(TransactionType type) {
        return dbHelper.getTransactions(type);
    }

    public void insertTransaction(Transaction transaction) {
        dbHelper.insertTransactions(transaction);
    }

}
