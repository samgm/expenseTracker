package com.antso.expensesmanager;

/**
 * Created by asolano on 5/4/2014.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expensesmanager.entities.Account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class AccountListAdapter extends BaseAdapter {

    private final List<Account> accounts;
    private final Context context;

    public AccountListAdapter(Context context, Collection<Account> accounts) {
        this.context = context;
        this.accounts = new ArrayList<Account>(accounts);
    }

    public void add(Account item) {

        accounts.add(item);
        notifyDataSetChanged();
    }

    public void del(int index) {
        accounts.remove(index);
        notifyDataSetChanged();
    }


    public void clear() {
        accounts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int pos) {
        return accounts.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the current ToDoItem
        final Account account = accounts.get(position);

        //Inflate the View for this ToDoItem
        // from todo_item.xml.
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.account_item, null, false);

        // Fill in specific ToDoItem data
        // Remember that the data that goes in this View
        // corresponds to the user interface elements defined
        // in the layout file

        // Display Title in TextView
        final LinearLayout colorView = (LinearLayout) itemLayout.findViewById(R.id.accountColor);
        colorView.setBackgroundColor(account.getColor());

        final TextView nameView = (TextView) itemLayout.findViewById(R.id.accountName);
        nameView.setText(account.getName());
        nameView.setTextColor(Color.BLACK);

        final TextView balanceView = (TextView) itemLayout.findViewById(R.id.accountBalance);
        String balance = account.getCurrency() + " " + account.getBalance().setScale(2).toPlainString();
        balanceView.setText(balance);
        if(account.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            balanceView.setTextColor(Color.GREEN);
        } else {
            balanceView.setTextColor(Color.RED);
        }

        final TextView monthInView = (TextView) itemLayout.findViewById(R.id.accountMonthIn);
        String monthIn = "Revenues " + account.getCurrency() + " " + account.getMonthIn().setScale(2).toPlainString();
        monthInView.setText(monthIn);
        monthInView.setTextColor(Color.GREEN);

        final TextView monthOutView = (TextView) itemLayout.findViewById(R.id.accountMonthOut);
        String monthOut = "Expenses " + account.getCurrency() + " " + account.getMonthOut().setScale(2).toPlainString();
        monthOutView.setText(monthOut);
        monthOutView.setTextColor(Color.RED);

        final TextView monthBalanceView = (TextView) itemLayout.findViewById(R.id.accountMonthBalance);
        String monthBalance = "Total " + account.getCurrency() + " " + account.getMonthBalance().setScale(2).toPlainString();
        monthBalanceView.setText(monthBalance);
        if (account.getMonthBalance().compareTo(BigDecimal.ZERO) > 0) {
            monthBalanceView.setTextColor(Color.GREEN);
        } else if (account.getMonthBalance().compareTo(BigDecimal.ZERO) < 0) {
            monthBalanceView.setTextColor(Color.RED);
        } else {
            monthBalanceView.setTextColor(Color.GRAY);
        }
        // Return the View you just created
        return itemLayout;

    }

}
