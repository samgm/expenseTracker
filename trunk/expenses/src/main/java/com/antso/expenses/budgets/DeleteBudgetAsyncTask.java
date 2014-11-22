package com.antso.expenses.budgets;

import android.app.Activity;
import android.widget.Toast;

import com.antso.expenses.R;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.BaseAsyncTaskWithProgress;

public class DeleteBudgetAsyncTask extends BaseAsyncTaskWithProgress<Budget> {

    private final Budget budget;
    private final String replaceWithId;

    public DeleteBudgetAsyncTask(final Activity activity, final int message, final Budget budget, final String replaceWithId) {
        super(activity, message);
        this.budget = budget;
        this.replaceWithId = replaceWithId;

    }

    @Override
    protected Budget doInBackground(Void... params) {
        BudgetManager.BUDGET_MANAGER().removeBudget(budget);
        TransactionManager.TRANSACTION_MANAGER()
                .replaceBudget(budget.getId(), replaceWithId);
        return budget;
    }

    @Override
    protected void onPostExecute(Budget budget) {
        super.onPostExecute(budget);
        Toast.makeText(activity, budget.getName() +
                        activity.getText(R.string.message_budget_deleted),
                Toast.LENGTH_LONG).show();
    }
}
