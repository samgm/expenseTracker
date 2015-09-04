package com.antso.expenses.transactions;

import com.antso.expenses.entities.Transaction;

public class CompoundedTransferTransaction {

    private final Transaction outTransaction;
    private final Transaction inTransaction;
    private volatile Transaction feeTransaction;

    public CompoundedTransferTransaction(final Transaction outTransaction,
                                         final Transaction inTransaction) {
        this.outTransaction = outTransaction;
        this.inTransaction = inTransaction;
        this.feeTransaction = null;
    }

    public CompoundedTransferTransaction(final Transaction outTransaction,
                                         final Transaction inTransaction,
                                         final Transaction feeTransaction) {
        this.outTransaction = outTransaction;
        this.inTransaction = inTransaction;
        this.feeTransaction = feeTransaction;
    }

    public Transaction getOutTransaction() {
        return outTransaction;
    }

    public Transaction getInTransaction() {
        return inTransaction;
    }

    public Transaction getFeeTransaction() {
        return feeTransaction;
    }

    public void setFeeTransaction(Transaction feeTransaction) {
        this.feeTransaction = feeTransaction;
    }
}