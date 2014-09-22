package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;

import org.joda.time.DateTime;


public class RevenuesTransactionListAdapter extends BaseAdapter {

    private final TransactionManager transactionManager;
    private final Context context;

    public RevenuesTransactionListAdapter(Context context, TransactionManager transactionManager) {
        this.context = context;
        this.transactionManager = transactionManager;
    }

    @Override
    public int getCount() {
        return transactionManager.getInTransactions().size();
    }

    @Override
    public Object getItem(int pos) {
        return transactionManager.getInTransactions().get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Transaction transaction = transactionManager.getInTransactions().get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout transactionLayout = (LinearLayout) inflater.inflate(R.layout.transaction_item, null, false);

        if (transaction.isAutoGenerated()) {
            transactionLayout.setBackgroundColor(MaterialColours.GREY_500);
        }

        AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER.getAccountInfo(transaction.getAccountId());
        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER.getBudgetInfo(transaction.getBudgetId());

        if(accountInfo != null) {
            Account account = accountInfo.account;
            final TextView accountName = (TextView) transactionLayout.findViewById(R.id.accountName);
            accountName.setText(account.getName());
            accountName.setTextColor(MaterialColours.BLACK);

            final CircleSectorView accountColor = (CircleSectorView) transactionLayout.findViewById(R.id.accountColor);
            accountColor.setColor(account.getColor());
        }

        if(budgetInfo != null) {
            Budget budget = budgetInfo.budget;
            final TextView budgetName = (TextView) transactionLayout.findViewById(R.id.budgetName);
            budgetName.setText(budget.getName());
            budgetName.setTextColor(MaterialColours.BLACK);

            final CircleSectorView budgetColor = (CircleSectorView) transactionLayout.findViewById(R.id.budgetColor);
            budgetColor.setColor(budget.getColor());
        }

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        DateTime d = transaction.getDateTime();
        String dateTime = d.getYear() + "-" + d.getMonthOfYear() +  "-" + d.getDayOfMonth();
        transactionDateTime.setText(dateTime);
        transactionDateTime.setTextColor(MaterialColours.BLACK);

        final ImageView transactionRecurrent = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
        if (transaction.getType().equals(TransactionType.Recurrent) && !transaction.isAutoGenerated()) {
            transactionRecurrent.setVisibility(View.VISIBLE);
        } else {
            transactionRecurrent.setVisibility(View.INVISIBLE);
        }

        final TextView transactionDesc = (TextView) transactionLayout.findViewById(R.id.transactionDesc);
        transactionDesc.setText(transaction.getDescription());
        transactionDesc.setTextColor(MaterialColours.BLACK);

        final TextView transactionValue = (TextView) transactionLayout.findViewById(R.id.transactionValue);
        String balance = Utils.getCurrencyString() + " " +
                transaction.getValue().setScale(2).toPlainString();
        transactionValue.setText(balance);
        if(transaction.getDirection().equals(TransactionDirection.In)) {
            transactionValue.setTextColor(MaterialColours.GREEN_500);
        } else if(transaction.getDirection().equals(TransactionDirection.Out)) {
            transactionValue.setTextColor(MaterialColours.RED_500);
        } else {
            transactionValue.setTextColor(MaterialColours.YELLOW_500);
        }

        return transactionLayout;
    }

}
