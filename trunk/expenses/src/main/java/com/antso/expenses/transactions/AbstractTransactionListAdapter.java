package com.antso.expenses.transactions;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.BaseAdapter;

import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public abstract class AbstractTransactionListAdapter<T> extends BaseAdapter implements Observer {
    protected final Context context;
    protected final HandlingFooterFragment fragment;
    protected volatile List<T> transactions;
    private final List<T> found;
    private boolean searched = false;
    private boolean accountManagerStarted;
    private boolean budgetManagerStarted;
    private boolean transactionManagerStarted;

    public AbstractTransactionListAdapter(Context context, HandlingFooterFragment fragment) {
        this.fragment = fragment;
        this.context = context;
        this.transactions = Collections.emptyList();
        this.found = new ArrayList<T>();

        Log.i("EXPENSES OBS", this.getClass() + " registered observer to (" +
                TransactionManager.TRANSACTION_MANAGER() + ")");

        TransactionManager.TRANSACTION_MANAGER().addObserver(this);
        AccountManager.ACCOUNT_MANAGER().addObserver(this);
        BudgetManager.BUDGET_MANAGER().addObserver(this);
    }

    protected abstract List<T> retrieveTransactions();
    protected abstract String getDescription(T t);

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

    public void search(String text) {
        found.clear();
        for (T t : transactions) {
            if (getDescription(t).toUpperCase().contains(text.toUpperCase())) {
                found.add(t);
            }
        }

        searched = true;
        transactions = found;
        notifyDataSetChanged();
    }

    public void resetSearch() {
        if (searched) {
            transactions = retrieveTransactions();
            notifyDataSetChanged();
            found.clear();
            searched = false;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof TransactionManager) {
            TransactionUpdateEvent event = (TransactionUpdateEvent) data;
            switch (event.reason) {
                case START:
                    if (transactions.size() == 0) {
                        Log.i("EXPENSES OBS", this.getClass() + " received TransactionManager START");
                        transactions = retrieveTransactions();
                        transactionManagerStarted = true;
                        if (transactionManagerStarted &&
                                accountManagerStarted &&
                                budgetManagerStarted) {
                            notifyDataSetChangedInUIThread();
                        }
                    }
                    break;

                case ADD:
                case UPD:
                case DEL:
                    Log.i("EXPENSES OBS", this.getClass() + " received UPD");
                    transactions = retrieveTransactions();
                    notifyDataSetChangedInUIThread();
                    break;
            }
        }

        if (observable instanceof AccountManager) {
            TransactionUpdateEvent event = (TransactionUpdateEvent) data;
            if (event.reason.equals(TransactionUpdateEvent.Reason.START)) {
                accountManagerStarted = true;
                Log.i("EXPENSES OBS", this.getClass() + " received AccountManager START");
                if (transactionManagerStarted && accountManagerStarted && budgetManagerStarted) {
                    notifyDataSetChangedInUIThread();
                }
            }
        }

        if (observable instanceof BudgetManager) {
            TransactionUpdateEvent event = (TransactionUpdateEvent) data;
            if (event.reason.equals(TransactionUpdateEvent.Reason.START)) {
                budgetManagerStarted = true;
                Log.i("EXPENSES OBS", this.getClass() + " received BudgetManager START");
                if (transactionManagerStarted && accountManagerStarted && budgetManagerStarted) {
                    notifyDataSetChangedInUIThread();
                }
            }
        }

    }

    private void notifyDataSetChangedInUIThread() {
        Log.i("EXPENSES OBS", this.getClass() + " calling  notifyDataSetChangedInUIThread");

        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getCount() == 0) {
                    fragment.showFooter();
                } else {
                    fragment.hideFooter();
                }

                notifyDataSetChanged();
            }
        });
    }

    public void onDestroy() {
        Log.i("EXPENSES OBS", this.getClass() + " deleted observer (" +
                TransactionManager.TRANSACTION_MANAGER() + ")");
        TransactionManager.TRANSACTION_MANAGER().deleteObserver(this);
    }
}
