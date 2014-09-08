package com.antso.expensesmanager.transactions;

/**
 * Created by asolano on 5/4/2014.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.entities.TransactionDirection;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TransactionListAdapter extends BaseAdapter {

    private final List<Transaction> transactions;
    private final Context context;

    public TransactionListAdapter(Context context, Collection<Transaction> transactions) {
        this.context = context;
        this.transactions = new ArrayList<Transaction>(transactions);
    }

    public void add(Transaction item) {

        transactions.add(item);
        notifyDataSetChanged();
    }

    public void del(int index) {
        transactions.remove(index);
        notifyDataSetChanged();
    }

    public void clear() {
        transactions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Object getItem(int pos) {
        return transactions.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the current ToDoItem
        final Transaction transaction = transactions.get(position);

        //Inflate the View for this ToDoItem
        // from todo_item.xml.
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.transaction_item, null, false);

        // Fill in specific ToDoItem data
        // Remember that the data that goes in this View
        // corresponds to the user interface elements defined
        // in the layout file

        // Display Title in TextView
        final TextView accountNameView = (TextView) itemLayout.findViewById(R.id.accountName);
        accountNameView.setText(transaction.getAccountId());
        accountNameView.setTextColor(Color.BLACK);

        final TextView transactionDateTimeView = (TextView) itemLayout.findViewById(R.id.transactionDateTime);
        DateTime d = transaction.getDateTime();
        String dateTime = d.getYear() + "-" + d.getMonthOfYear() +  "-" + d.getDayOfMonth() + " "
                + d.getHourOfDay() + ":" + d.getMinuteOfHour();
        transactionDateTimeView.setText(dateTime);
        transactionDateTimeView.setTextColor(Color.BLUE);

        final TextView transactionDescView = (TextView) itemLayout.findViewById(R.id.transactionDesc);
        transactionDescView.setText(transaction.getDescription());
        transactionDescView.setTextColor(Color.BLACK);

        final TextView transactionValueView = (TextView) itemLayout.findViewById(R.id.transactionValue);
        String balance = transaction.getValue().setScale(2).toPlainString();
        transactionValueView.setText(balance);
        if(transaction.getDirection().equals(TransactionDirection.In)) {
            transactionValueView.setTextColor(Color.GREEN);
        } else if(transaction.getDirection().equals(TransactionDirection.Out)) {
            transactionValueView.setTextColor(Color.RED);
        } else {
            transactionValueView.setTextColor(Color.YELLOW);
        }

        // Return the View you just created
        return itemLayout;

    }

}
