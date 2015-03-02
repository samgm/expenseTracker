package com.antso.expenses.utils;

import com.antso.expenses.accounts.AccountManager;

import java.util.Comparator;

public class AccountInfoAlphabeticalComparator implements Comparator<AccountManager.AccountInfo> {

    @Override
    public int compare(AccountManager.AccountInfo lhs, AccountManager.AccountInfo rhs) {
        return lhs.account.getName().compareTo(rhs.account.getName());
    }
}
