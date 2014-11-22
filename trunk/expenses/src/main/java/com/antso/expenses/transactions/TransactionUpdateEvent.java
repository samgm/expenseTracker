package com.antso.expenses.transactions;

import com.antso.expenses.entities.Transaction;

public class TransactionUpdateEvent {

    enum Reason {
        UNDEF,
        START,
        ADD,
        UPD,
        DEL
    }

    public Reason reason;
    public Transaction[] data;
    public Transaction newData;
    private static TransactionUpdateEvent START = new TransactionUpdateEvent(Reason.START, null);

    private TransactionUpdateEvent(Reason reason, Transaction... data) {
        this.reason = reason;
        this.data = data;
    }

    public static TransactionUpdateEvent createStart() {
        return START;
    }

    public static TransactionUpdateEvent createAdd(Transaction... data) {
        return new TransactionUpdateEvent(Reason.ADD, data);
    }

    public static TransactionUpdateEvent createUpd(Transaction... data) {
        return new TransactionUpdateEvent(Reason.UPD, data);
    }

    public static TransactionUpdateEvent createDel(Transaction... data) {
        return new TransactionUpdateEvent(Reason.DEL, data);
    }
}
