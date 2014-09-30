package com.antso.expensesmanager.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.antso.expensesmanager.entities.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {

    private Map<String, Integer> idToIndex;

    public AccountSpinnerAdapter(Context context, int resource, Account[] objects) {
        super(context, resource, objects);

        idToIndex = new HashMap<String, Integer>(objects.length);
        int i = 0;
        for (Account account : objects) {
            idToIndex.put(account.getId(), i);
            i++;
        }
    }

    public int getIndexById(String id) {
        return idToIndex.get(id);
    }
}
