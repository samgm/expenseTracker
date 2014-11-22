package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antso.expensesmanager.entities.SummaryTransaction;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.utils.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BudgetTransactionListAdapter extends BaseAccountBudgetTransactionListAdapter {

    private final TransactionManager transactionManager;
    private final Context context;
    private final String budget;
    private boolean nothingMoreToLoad = false;

    private List<Transaction> transactions = new ArrayList<Transaction>();

    public BudgetTransactionListAdapter(Context context, TransactionManager transactionManager, String budget) {
        super(context);
        this.context = context;
        this.budget = budget;
        this.transactionManager = transactionManager;
        transactionManager.resetGetBudgetNextPeriodTransactions(Utils.now());
        load();
    }

    @Override
    public void load() {
        if(nothingMoreToLoad) {
            return;
        }

        new AsyncTask<Void, Void,  List<Transaction>>() {
            @Override
            protected List<Transaction> doInBackground(Void... params) {
                return transactionManager.getBudgetNextPeriodTransactions(budget);
            }

            @Override
            protected void onPostExecute(List<Transaction> loaded) {
                super.onPostExecute(transactions);
                if (loaded.size() != 0) {
                    transactions.addAll(loaded);
                    notifyDataSetChanged();
                } else {
                    nothingMoreToLoad = true;
                }
            }
        }.execute();
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
        final Transaction transaction = transactions.get(position);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (transaction instanceof SummaryTransaction) {
            return createSummaryTransactionView(inflater, context, (SummaryTransaction) transaction);
        } else {
            return createTransactionView(inflater, context, transaction);
        }
    }

}
