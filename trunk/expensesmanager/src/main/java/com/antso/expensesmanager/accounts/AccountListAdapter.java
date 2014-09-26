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
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;

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
        final AccountManager.AccountInfo accountInfo = accountManager.getAccountInfo().get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout accountLayout = (LinearLayout) inflater.inflate(R.layout.account_item, null, false);

        final CircleSectorView color = (CircleSectorView) accountLayout.findViewById(R.id.accountColor);
        color.setColor(accountInfo.account.getColor());

        final TextView name = (TextView) accountLayout.findViewById(R.id.accountName);
        name.setText(accountInfo.account.getName());
        name.setTextColor(MaterialColours.BLACK);

        final TextView balance = (TextView) accountLayout.findViewById(R.id.accountBalance);
        String balanceStr = Utils.getCurrencyString() + " " +
                accountInfo.balance.setScale(2).toPlainString();
        balance.setText(balanceStr);
        if(accountInfo.balance.compareTo(BigDecimal.ZERO) >= 0) {
            balance.setTextColor(MaterialColours.GREEN_500);
        } else {
            balance.setTextColor(MaterialColours.RED_500);
        }

        final TextView monthIn = (TextView) accountLayout.findViewById(R.id.accountMonthIn);
        String monthInStr = Utils.getCurrencyString() + " " +
                accountInfo.monthIn.setScale(2).toPlainString();
        monthIn.setText(monthInStr);
        monthIn.setTextColor(MaterialColours.GREEN_500);

        final TextView monthOut = (TextView) accountLayout.findViewById(R.id.accountMonthOut);
        String monthOutStr = Utils.getCurrencyString() + " " +
                accountInfo.monthOut.setScale(2).toPlainString();
        monthOut.setText(monthOutStr);
        monthOut.setTextColor(MaterialColours.RED_500);

        final TextView monthBalance = (TextView) accountLayout.findViewById(R.id.accountMonthBalance);
        String monthBalanceStr = Utils.getCurrencyString() + " " +
                accountInfo.monthBalance.setScale(2).toPlainString();
        monthBalance.setText(monthBalanceStr);
        if (accountInfo.monthBalance.compareTo(BigDecimal.ZERO) > 0) {
            monthBalance.setTextColor(MaterialColours.GREEN_500);
        } else if (accountInfo.monthBalance.compareTo(BigDecimal.ZERO) < 0) {
            monthBalance.setTextColor(MaterialColours.RED_500);
        } else {
            monthBalance.setTextColor(MaterialColours.GREY_500);
        }

        return accountLayout;
    }
}
