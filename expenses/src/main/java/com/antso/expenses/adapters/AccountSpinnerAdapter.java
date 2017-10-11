package com.antso.expenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Account;
import com.antso.expenses.views.CircleSectorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {

    private Map<String, Integer> idToIndex;
    private LayoutInflater mInflater;


    protected AccountSpinnerAdapter(Context context, int resource, Account[] accounts) {
        super(context, resource, accounts);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        ArrayList<Account> finalAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (!account.isArchived()) {
                finalAccounts.add(account);
            }
        }

        return new AccountSpinnerAdapter(context, resource, finalAccounts.toArray(new Account[0]));
    }

    public static AccountSpinnerAdapter create(Context context, int resource, Account[] accounts,
                                               Account excludeAccount) {
        ArrayList<Account> finalAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (!account.isArchived() && !account.getId().equals(excludeAccount.getId())) {
                finalAccounts.add(account);
            }
        }

        return new AccountSpinnerAdapter(context, resource, finalAccounts.toArray(new Account[0]));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.account_spinner_item,
                R.id.accountSpinnerItemText,
                R.id.accountSpinnerItemColor);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.account_spinner_item,
                R.id.accountSpinnerItemText,
                R.id.accountSpinnerItemColor);
    }

    private View createView(int position, View convertView, ViewGroup parent,
                            int viewId, int textId, int colorId) {
        View view = mInflater.inflate(viewId, parent, false);

        Account account = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(account.getName());

        CircleSectorView color = (CircleSectorView)view.findViewById(colorId);
        color.setColor(account.getColor());

        return view;
    }
}
