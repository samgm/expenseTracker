package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;

import org.joda.time.DateTime;


public class TransfersTransactionListAdapter extends BaseAdapter {

    private final TransactionManager transactionManager;
    private final Context context;

    public TransfersTransactionListAdapter(Context context, TransactionManager transactionManager) {
        this.context = context;
        this.transactionManager = transactionManager;
    }

    @Override
    public int getCount() {
        return transactionManager.getTransferTransactions().size();
    }

    @Override
    public Object getItem(int pos) {
        return transactionManager.getTransferTransactions().get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pair<Transaction, Transaction> pair = transactionManager.getTransferTransactions().get(position);
        final Transaction t1 = pair.first;
        final Transaction t2 = pair.second;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout transactionLayout = (LinearLayout) inflater.inflate(R.layout.transaction_item, null, false);

        LinearLayout accountAndAccount = (LinearLayout) transactionLayout.findViewById(R.id.accountAndAccountLayout);
        LinearLayout accountAndBudget = (LinearLayout) transactionLayout.findViewById(R.id.accountAndBudgetLayout);
        accountAndAccount.setVisibility(View.VISIBLE);
        accountAndBudget.setVisibility(View.GONE);

        AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER.getAccountInfo(t1.getAccountId());
//        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER.getBudgetInfo(t1.getBudgetId());

        if(accountInfo != null) {
            Account account = accountInfo.account;
            final TextView accountName = (TextView) transactionLayout.findViewById(R.id.accountName);
            accountName.setText(account.getName());
            accountName.setTextColor(MaterialColours.BLACK);

            final CircleSectorView accountColor = (CircleSectorView) transactionLayout.findViewById(R.id.accountColor);
            accountColor.setColor(account.getColor());
        }

//        if(budgetInfo != null) {
//            Budget budget = budgetInfo.budget;
//            final TextView budgetName = (TextView) transactionLayout.findViewById(R.id.budgetName);
//            budgetName.setText(budget.getName());
//            budgetName.setTextColor(MaterialColours..BLACK);
//
//            final CircleSectorView budgetColor = (CircleSectorView) transactionLayout.findViewById(R.id.budgetColor);
//            budgetColor.setColor(budget.getColor());
//        }

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        String dateTime = Utils.formatDate(t1.getDateTime());
        transactionDateTime.setText(dateTime);
        transactionDateTime.setTextColor(MaterialColours.BLACK);

        final ImageView transactionRecurrent = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
        transactionRecurrent.setVisibility(View.INVISIBLE);

        final TextView transactionDesc = (TextView) transactionLayout.findViewById(R.id.transactionDesc);
        transactionDesc.setText(t1.getDescription());
        transactionDesc.setTextColor(MaterialColours.BLACK);

        final TextView transactionValue = (TextView) transactionLayout.findViewById(R.id.transactionValue);
        String balance = Utils.getCurrencyString() + " " +
                t1.getValue().setScale(2).toPlainString();
        transactionValue.setText(balance);
        transactionValue.setTextColor(MaterialColours.BLACK);

        return transactionLayout;
    }

}
