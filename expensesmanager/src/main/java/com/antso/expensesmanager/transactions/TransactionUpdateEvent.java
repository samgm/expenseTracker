package com.antso.expensesmanager.transactions;

import com.antso.expensesmanager.entities.Transaction;

public class TransactionUpdateEvent {

    enum Reason {
        UNDEF,
        START,
        ADD,
        UPD,
        DEL
    }

    public Reason reason;
    public Transaction oldData;
    public Transaction newData;
    private static TransactionUpdateEvent START = new TransactionUpdateEvent(Reason.START, null, null);

    private TransactionUpdateEvent(Reason reason, Transaction oldData, Transaction newData) {
        this.reason = reason;
        this.oldData = oldData;
        this.newData = newData;
    }

    public static TransactionUpdateEvent createStart() {
        return START;
    }

    public static TransactionUpdateEvent createAdd(Transaction data) {
        return new TransactionUpdateEvent(Reason.ADD, null, data);
    }

    public static TransactionUpdateEvent createUpd(Transaction oldData, Transaction newData) {
        return new TransactionUpdateEvent(Reason.UPD, oldData, newData);
    }

    public static TransactionUpdateEvent createDel(Transaction data) {
        return new TransactionUpdateEvent(Reason.DEL, data, null);
    }
}
