package com.antso.expenses.transactions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.CircleSectorView;

import java.util.List;


public class ExpensesTransactionListAdapter
        extends AbstractTransactionListAdapter<Transaction> {

    public ExpensesTransactionListAdapter(Context context, HandlingFooterFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected List<Transaction> retrieveTransactions() {
        return TransactionManager.TRANSACTION_MANAGER().getOutTransactions();
    }

    @Override
    protected String getDescription(Transaction transaction) {
        return transaction.getDescription();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Transaction transaction = transactions.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout transactionLayout = (LinearLayout) inflater.inflate(R.layout.transaction_item, null, false);

        if (transaction.isAutoGenerated()) {
            transactionLayout.setBackgroundColor(MaterialColours.GREY_500);
        }

        AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER().getAccountInfo(transaction.getAccountId());
        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER().getBudgetInfo(transaction.getBudgetId());

        if(accountInfo != null) {
            Account account = accountInfo.account;
            final TextView accountName = (TextView) transactionLayout.findViewById(R.id.accountName);
            accountName.setText(account.getName());

            final CircleSectorView accountColor = (CircleSectorView) transactionLayout.findViewById(R.id.accountColor);
            accountColor.setColor(account.getColor());
        }

        if(budgetInfo != null) {
            Budget budget = budgetInfo.budget;
            final TextView budgetName = (TextView) transactionLayout.findViewById(R.id.budgetName);
            budgetName.setText(budget.getName());

            final CircleSectorView budgetColor = (CircleSectorView) transactionLayout.findViewById(R.id.budgetColor);
            budgetColor.setColor(budget.getColor());
        }

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        String dateTime = Utils.formatDate(transaction.getDate());
        transactionDateTime.setText(dateTime);

        final ImageView transactionRecurrent = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
        if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
            transactionRecurrent.setVisibility(View.VISIBLE);
        } else {
            transactionRecurrent.setVisibility(View.INVISIBLE);
        }

        final TextView transactionDesc = (TextView) transactionLayout.findViewById(R.id.transactionDesc);
        transactionDesc.setText(transaction.getDescription());

        final TextView transactionValue = (TextView) transactionLayout.findViewById(R.id.transactionValue);
        String balance = Utils.getCurrencyString(context) + " " +
                transaction.getValue().setScale(2).toPlainString();
        transactionValue.setText(balance);

        return transactionLayout;
    }
}
