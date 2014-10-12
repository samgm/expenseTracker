package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;


public class TransfersTransactionListAdapter
        extends AbstractTransactionListAdapter<Pair<Transaction, Transaction>> {

    public TransfersTransactionListAdapter(Context context, HandlingFooterFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected List<Pair<Transaction, Transaction>> retrieveTransactions() {
        return TransactionManager.TRANSACTION_MANAGER().getTransferTransactions();
    }

    @Override
    protected String getDescription(Pair<Transaction, Transaction> pair) {
        return pair.first.getDescription();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pair<Transaction, Transaction> pair = transactions.get(position);
        final Transaction t1 = pair.first;
        final Transaction t2 = pair.second;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout transactionLayout = (LinearLayout) inflater.inflate(R.layout.transaction_item, null, false);

        LinearLayout accountAndAccount = (LinearLayout) transactionLayout.findViewById(R.id.accountAndAccountLayout);
        LinearLayout accountAndBudget = (LinearLayout) transactionLayout.findViewById(R.id.accountAndBudgetLayout);
        accountAndAccount.setVisibility(View.VISIBLE);
        accountAndBudget.setVisibility(View.GONE);

        AccountManager.AccountInfo accountPrimaryInfo =
                AccountManager.ACCOUNT_MANAGER().getAccountInfo(t1.getAccountId());
        AccountManager.AccountInfo accountSecondaryInfo =
                AccountManager.ACCOUNT_MANAGER().getAccountInfo(t2.getAccountId());

        if(accountPrimaryInfo != null) {
            Account account = accountPrimaryInfo.account;
            final TextView accountPrimaryName =
                    (TextView) transactionLayout.findViewById(R.id.accountPrimaryName);
            accountPrimaryName.setText(account.getName());
            accountPrimaryName.setTextColor(MaterialColours.BLACK);

            final CircleSectorView accountPrimaryColor =
                    (CircleSectorView) transactionLayout.findViewById(R.id.accountPrimaryColor);
            accountPrimaryColor.setColor(account.getColor());
        }

        if(accountSecondaryInfo != null) {
            Account account = accountSecondaryInfo.account;
            final TextView accountSecondaryName =
                    (TextView) transactionLayout.findViewById(R.id.accountSecondaryName);
            accountSecondaryName.setText(account.getName());
            accountSecondaryName.setTextColor(MaterialColours.BLACK);

            final CircleSectorView accountSecondaryColor =
                    (CircleSectorView) transactionLayout.findViewById(R.id.accountSecondaryColor);
            accountSecondaryColor.setColor(account.getColor());
        }

        final TextView transactionDateTime = (TextView) transactionLayout.findViewById(R.id.transactionDateTime);
        String dateTime = Utils.formatDate(t1.getDate());
        transactionDateTime.setText(dateTime);
        transactionDateTime.setTextColor(MaterialColours.BLACK);

        final ImageView transactionRecurrent = (ImageView) transactionLayout.findViewById(R.id.transactionRecurrent);
        if (t1.getRecurrent() && !t1.isAutoGenerated()) {
            transactionRecurrent.setVisibility(View.VISIBLE);
        } else {
            transactionRecurrent.setVisibility(View.INVISIBLE);
        }

        final TextView transactionDesc = (TextView) transactionLayout.findViewById(R.id.transactionDesc);
        transactionDesc.setText(t1.getDescription());
        transactionDesc.setTextColor(MaterialColours.BLACK);

        final TextView transactionValue = (TextView) transactionLayout.findViewById(R.id.transactionValue);
        String balance = Utils.getCurrencyString(context) + " " +
                t1.getValue().setScale(2).toPlainString();
        transactionValue.setText(balance);
        transactionValue.setTextColor(MaterialColours.BLACK);

        return transactionLayout;
    }
}
