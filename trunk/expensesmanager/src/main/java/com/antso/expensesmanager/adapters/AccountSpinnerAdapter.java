package com.antso.expensesmanager.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.antso.expensesmanager.entities.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {

    private Map<String, Integer> idToIndex;

    protected AccountSpinnerAdapter(Context context, int resource, Account[] accounts) {
        super(context, resource, accounts);

        idToIndex = new HashMap<String, Integer>(accounts.length);
        int i = 0;
        for (Account account : accounts) {
            idToIndex.put(account.getId(), i);
            i++;
        }
    }

    public int getIndexById(String id) {
        if(id == null) {
            return 0;
        }
        Integer index = idToIndex.get(id);
        return (index != null) ? index : 0;
    }

    public static AccountSpinnerAdapter create(Context context, int resource, Account[] accounts) {
        return new AccountSpinnerAdapter(context, resource, accounts);
    }

    public static AccountSpinnerAdapter create(Context context, int resource, Account[] accounts,
                                               Account excludeAccount) {
        Account[] finalAccounts = new Account[accounts.length - 1];
        int i = 0;
        for (Account account : accounts) {
            if (!account.getId().equals(excludeAccount.getId()) && i < finalAccounts.length) {
                finalAccounts[i] = account;
                i++;
            }
        }

        return new AccountSpinnerAdapter(context, resource, finalAccounts);
    }

}
