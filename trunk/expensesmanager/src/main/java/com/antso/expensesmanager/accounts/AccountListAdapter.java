package com.antso.expensesmanager.accounts;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.Utils;

import java.math.BigDecimal;
import java.util.List;


public class AccountListAdapter extends BaseAdapter {
    private final AccountManager accountManager;
    private final Context context;

    public AccountListAdapter(Context context, AccountManager accountManager) {
        this.context = context;
        this.accountManager = accountManager;
    }

    @Override
    public int getCount() {
        return accountManager.getAccountInfo().size();
    }

    @Override
    public Object getItem(int pos) {
        return accountManager.getAccountInfo().get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the current ToDoItem
        final AccountManager.AccountInfo accountInfo = accountManager.getAccountInfo().get(position);

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
        colorView.setBackgroundColor(accountInfo.account.getColor());

        final TextView nameView = (TextView) itemLayout.findViewById(R.id.accountName);
        nameView.setText(accountInfo.account.getName());
        nameView.setTextColor(Color.BLACK);

        final TextView balanceView = (TextView) itemLayout.findViewById(R.id.accountBalance);
        String balance = Utils.getCurrencyString() + " " + accountInfo.balance.setScale(2).toPlainString();
        balanceView.setText(balance);
        if(accountInfo.balance.compareTo(BigDecimal.ZERO) >= 0) {
            balanceView.setTextColor(Color.GREEN);
        } else {
            balanceView.setTextColor(Color.RED);
        }

        final TextView monthInView = (TextView) itemLayout.findViewById(R.id.accountMonthIn);
        String monthIn = "Revenues " + Utils.getCurrencyString() + " " +
                accountInfo.monthIn.setScale(2).toPlainString();
        monthInView.setText(monthIn);
        monthInView.setTextColor(Color.GREEN);

        final TextView monthOutView = (TextView) itemLayout.findViewById(R.id.accountMonthOut);
        String monthOut = "Expenses " + Utils.getCurrencyString() + " " + accountInfo.monthOut.setScale(2).toPlainString();
        monthOutView.setText(monthOut);
        monthOutView.setTextColor(Color.RED);

        final TextView monthBalanceView = (TextView) itemLayout.findViewById(R.id.accountMonthBalance);
        String monthBalance = "Total " + Utils.getCurrencyString() + " " + accountInfo.monthBalance.setScale(2).toPlainString();
        monthBalanceView.setText(monthBalance);
        if (accountInfo.monthBalance.compareTo(BigDecimal.ZERO) > 0) {
            monthBalanceView.setTextColor(Color.GREEN);
        } else if (accountInfo.monthBalance.compareTo(BigDecimal.ZERO) < 0) {
            monthBalanceView.setTextColor(Color.RED);
        } else {
            monthBalanceView.setTextColor(Color.GRAY);
        }
        // Return the View you just created
        return itemLayout;

    }
}
