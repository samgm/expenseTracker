package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.BaseAsyncTaskWithProgress;

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
        Toast.makeText(activity, account.getName() +
                        activity.getText(R.string.message_account_deleted),
                Toast.LENGTH_LONG).show();
        }
}
