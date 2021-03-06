package com.antso.expenses.transactions;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.utils.Utils;

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
                boolean finished = false;
                List<Transaction> transactions = transactionManager.getBudgetNextPeriodTransactions(budget, finished);
                nothingMoreToLoad = finished;
                return transactions;
            }

            @Override
            protected void onPostExecute(List<Transaction> loaded) {
                super.onPostExecute(transactions);
                transactions.addAll(loaded);
                notifyDataSetChanged();
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
            View transactionLayout = createSummaryTransactionView(inflater, context, (SummaryTransaction) transaction);
            final TextView transactionValueIn = (TextView) transactionLayout.findViewById(R.id.transactionDescNet);
            transactionValueIn.setText(R.string.net_with_N_label);
            return transactionLayout;
        } else {
            return createTransactionView(inflater, context, transaction);
        }
    }

}
