package com.antso.expenses.transactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Settings;
import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public abstract class BaseAccountBudgetTransactionListAdapter extends BaseAdapter {
    private boolean useMultiline;

    protected BaseAccountBudgetTransactionListAdapter(Context context){
        useMultiline = Settings.getMultilineDescriptionInTransactionList(context);
    }
    public abstract void load();

    protected View createTransactionView(LayoutInflater inflater, Context context, Transaction transaction) {
        RelativeLayout transactionLayout = (RelativeLayout) inflater.inflate(R.layout.transaction_item_small, null, false);

        if (transaction.isAutoGenerated()) {
            transactionLayout.setBackgroundColor(MaterialColours.GREY_300);
        }

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        DateTime d = transaction.getDate();
        if (transaction.getType().equals(TransactionType.Summary)) {
            transactionDateTime.setText(Utils.formatDateMonthYearOnly(d));
        } else {
            transactionDateTime.setText(Utils.formatDate(d));
        }

        final ImageView icon = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
        if (transaction.getType().equals(TransactionType.Transfer)) {
            if (transaction.getRecurrent()) {
                icon.setImageResource(R.drawable.ic_swap_event);
            }else {
                icon.setImageResource(R.drawable.ic_swap);
            }
        } else if (transaction.getType().equals(TransactionType.Fee)) {
            icon.setImageResource(R.drawable.ic_fee);
        }else if (transaction.getRecurrent()) {
            icon.setImageResource(R.drawable.ic_event);
        } else {
            icon.setVisibility(View.INVISIBLE);
        }

        final TextView transactionDesc = (TextView) transactionLayout.findViewById(R.id.transactionDesc);
        transactionDesc.setSingleLine(!useMultiline);
        transactionDesc.setText(transaction.getDescription());

        final TextView transactionCurrency = (TextView) transactionLayout.findViewById(R.id.transactionCurrency);
        transactionCurrency.setText(Utils.getCurrencyString(context));

        final TextView transactionValue = (TextView) transactionLayout.findViewById(R.id.transactionValue);
        String balance = transaction.getValue().setScale(2).toPlainString();
        transactionValue.setText(balance);
        if (transaction.getDirection().equals(TransactionDirection.In)) {
            transactionValue.setTextColor(MaterialColours.GREEN_500);
        } else if (transaction.getDirection().equals(TransactionDirection.Out)) {
            transactionValue.setTextColor(MaterialColours.RED_500);
        }

        return transactionLayout;
    }

    protected View createSummaryTransactionView(LayoutInflater inflater, Context context, SummaryTransaction transaction) {
        LinearLayout transactionLayout = (LinearLayout) inflater.inflate(R.layout.transaction_item_summary, null, false);

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        DateTime d = transaction.getDate();
        if (transaction.getType().equals(TransactionType.Summary)) {
            transactionDateTime.setText(Utils.formatDateMonthYearOnly(d));
        } else {
            transactionDateTime.setText(Utils.formatDate(d));
        }

//        final ImageView icon = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
//        if (transaction.getType().equals(TransactionType.Transfer)) {
//            icon.setImageResource(R.drawable.ic_action_import_export);
//        } else  if (transaction.getRecurrent()) {
//            icon.setImageResource(R.drawable.ic_action_refresh);
//        } else {
//            icon.setVisibility(View.INVISIBLE);
//        }

        final TextView currencyIn = (TextView) transactionLayout.findViewById(R.id.transactionCurrencyIn);
        final TextView currencyOut = (TextView) transactionLayout.findViewById(R.id.transactionCurrencyOut);
        final TextView currencyBalance = (TextView) transactionLayout.findViewById(R.id.transactionCurrencyBalance);
        final TextView currencyDiff = (TextView) transactionLayout.findViewById(R.id.transactionCurrencyDiff);
        String c = Utils.getCurrencyString(context);
        currencyIn.setText(c);
        currencyOut.setText(c);
        currencyBalance.setText(c);
        currencyDiff.setText(c);

        final TextView transactionValueIn = (TextView) transactionLayout.findViewById(R.id.transactionValueIn);
        final TextView transactionValueOut = (TextView) transactionLayout.findViewById(R.id.transactionValueOut);
        final TextView transactionValueBalance = (TextView) transactionLayout.findViewById(R.id.transactionValueBalance);
        final TextView transactionValueDiff = (TextView) transactionLayout.findViewById(R.id.transactionValueDiff);
        String in = transaction.getValueIn().setScale(2).toPlainString();
        String out = transaction.getValueOut().setScale(2).toPlainString();
        String diff = transaction.getValueDiff().setScale(2).toPlainString();
        String balance = transaction.getBalance().setScale(2).toPlainString();
        transactionValueIn.setText(in);
        transactionValueOut.setText(out);
        transactionValueBalance.setText(balance);
        if (transaction.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            transactionValueBalance.setTextColor(MaterialColours.GREEN_500);
        } else {
            transactionValueBalance.setTextColor(MaterialColours.RED_500);
        }
        transactionValueDiff.setText(diff);
        if (transaction.getValueDiff().compareTo(BigDecimal.ZERO) > 0) {
            transactionValueDiff.setTextColor(MaterialColours.GREEN_500);
        } else if (transaction.getValueDiff().compareTo(BigDecimal.ZERO) < 0) {
            transactionValueDiff.setTextColor(MaterialColours.RED_500);
        } else {
            transactionValueDiff.setVisibility(View.INVISIBLE);
            currencyDiff.setVisibility(View.INVISIBLE);
        }

        return transactionLayout;
    }
}
