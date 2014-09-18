package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.views.CircleSectorView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class TransactionListAdapter extends BaseAdapter {

    private final List<Transaction> transactions;
    private final Context context;

    public TransactionListAdapter(Context context, Collection<Transaction> transactions) {
        this.context = context;
        this.transactions = new ArrayList<Transaction>(transactions);
        Collections.sort(this.transactions, new TransactionByDateComparator());
    }

    public void add(Transaction item) {
        //TODO I may need to add at the top of the list instead than at the end
        transactions.add(item);
        Collections.sort(this.transactions, new TransactionByDateComparator());
        notifyDataSetChanged();
    }

    public void del(int index) {
        Transaction transaction = transactions.get(index);
        if(transaction.getLinkedTransactionId().isEmpty()) {
            transactions.remove(index);
        } else {
            int linkedIndex = getLinkedTransactionIndex(transaction.getLinkedTransactionId(), index);
            if (linkedIndex > index) {
                transactions.remove(linkedIndex);
                transactions.remove(index);
            } else {
                transactions.remove(index);
                transactions.remove(linkedIndex);
            }
        }
        notifyDataSetChanged();
    }

    private int getLinkedTransactionIndex(String linkedTransactionId, int selectedIndex) {
        int beforeIndex = selectedIndex - 1;
        Transaction before = null;
        if (beforeIndex >=0 && beforeIndex < getCount()) {
            before = (Transaction) getItem(beforeIndex);
        }

        int afterIndex = selectedIndex + 1;
        Transaction after = null;
        if (afterIndex >=0 && afterIndex < getCount()) {
            after = (Transaction) getItem(afterIndex);
        }

        if(before != null && before.getId().equals(linkedTransactionId)) {
            return beforeIndex;
        }
        if(after != null && after.getId().equals(linkedTransactionId)) {
            return afterIndex;
        }

        return -1;
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

        AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER.getAccountInfo(transaction.getAccountId());
        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER.getBudgetInfo(transaction.getBudgetId());

        // Display Title in TextView
        if(accountInfo != null) {
            Account account = accountInfo.account;
            final TextView accountName = (TextView) itemLayout.findViewById(R.id.accountName);
            accountName.setText(account.getName());
            accountName.setTextColor(Color.BLACK);

            final CircleSectorView accountColor = (CircleSectorView) itemLayout.findViewById(R.id.accountColor);
            accountColor.setColor(account.getColor());
        }

        if(budgetInfo != null) {
            Budget budget = budgetInfo.budget;
            final TextView budgetName = (TextView) itemLayout.findViewById(R.id.budgetName);
            budgetName.setText(budget.getName());
            budgetName.setTextColor(Color.BLACK);

            final CircleSectorView budgetColor = (CircleSectorView) itemLayout.findViewById(R.id.budgetColor);
            budgetColor.setColor(budget.getColor());
        }

        final TextView transactionDateTimeView = (TextView) itemLayout.findViewById(R.id.transactionDateTime);
        DateTime d = transaction.getDateTime();
        String dateTime = d.getYear() + "-" + d.getMonthOfYear() +  "-" + d.getDayOfMonth();
        transactionDateTimeView.setText(dateTime);
        transactionDateTimeView.setTextColor(Color.BLACK);

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
