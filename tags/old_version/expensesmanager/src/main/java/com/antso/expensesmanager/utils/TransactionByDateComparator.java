package com.antso.expensesmanager.utils;

import com.antso.expensesmanager.entities.Transaction;

import java.util.Comparator;

public class TransactionByDateComparator implements Comparator<Transaction>{

    @Override
    public int compare(Transaction lhs, Transaction rhs) {
        return rhs.getDate().compareTo(lhs.getDate());
    }
}
