package com.antso.expenses.accounts;

import android.app.Activity;

import com.antso.expenses.entities.Account;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.BaseAsyncTaskWithProgress;
import com.antso.expenses.utils.Utils;

public class DeleteAccountAsyncTask
        extends BaseAsyncTaskWithProgress<Account> {

    private final Account account;
    private final String replaceWithId;

    public DeleteAccountAsyncTask(final Activity activity, final int message, final Account account, final String replaceWithId) {
        super(activity, message);
        this.account = account;
        this.replaceWithId = replaceWithId;
    }

    @Override
    protected Account doInBackground(Void... params) {
        AccountManager.ACCOUNT_MANAGER().removeAccount(account);
        if (replaceWithId != null && !replaceWithId.isEmpty()) {
            TransactionManager.TRANSACTION_MANAGER()
                    .replaceAccount(account.getId(), replaceWithId);
        } else {
            TransactionManager.TRANSACTION_MANAGER()
                    .removeTransactionByAccount(account.getId());
        }
        return account;
    }

    @Override
    protected void onPostExecute(Account account) {
        super.onPostExecute(account);
        Utils.showDeletedToast(activity, account.toString());
    }
}
