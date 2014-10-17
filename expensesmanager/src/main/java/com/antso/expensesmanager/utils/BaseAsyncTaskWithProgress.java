package com.antso.expensesmanager.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.transactions.TransactionManager;

public abstract class BaseAsyncTaskWithProgress<RES>
        extends AsyncTask<Void, Void, RES> {

    protected final Activity activity;
    private final int message;
    private ProgressDialog progress;

    public BaseAsyncTaskWithProgress(final Activity activity, final int message) {
        this.activity = activity;
        this.message = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progress = ProgressDialog.show(activity, "", activity.getText(message));
    }

    @Override
    protected void onPostExecute(RES res) {
        super.onPostExecute(res);
        this.progress.dismiss();
    }
}
