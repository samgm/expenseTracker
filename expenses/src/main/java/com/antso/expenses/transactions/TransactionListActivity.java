package com.antso.expenses.transactions;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.MaterialColours;

import com.antso.expenses.comapt.AppCompatListActivity;

// Used to show a list of activities, for example when clicking on an account
//this is used to show the list of all the activity related to that account

public class TransactionListActivity extends AppCompatListActivity {
    private BaseAccountBudgetTransactionListAdapter transactionListAdapter = null;
    private LinearLayout footerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.content).getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.tool_bar, root, false);
        root.addView(toolbar, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle params = getIntent().getExtras();
        String accountId = params.getString(IntentParamNames.ACCOUNT_ID);
        String budgetId = params.getString(IntentParamNames.BUDGET_ID);

        if(accountId != null) {
            setTitle(R.string.title_accounts_transaction_list_activity);
            transactionListAdapter = new AccountTransactionListAdapter(getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER(),
                    accountId);
        }
        if(budgetId != null) {
            setTitle(R.string.title_budgets_transaction_list_activity);
            transactionListAdapter = new BudgetTransactionListAdapter(getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER(),
                    budgetId);
        }

        footerView = (LinearLayout) getLayoutInflater().inflate(R.layout.list_footer, null, false);
        if (footerView != null && transactionListAdapter.getCount() == 0) {
            TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
            textView.setText(R.string.transaction_list_footer_text);
            textView.setTextColor(MaterialColours.GREY_500);

            getListView().addFooterView(footerView);
            getListView().setFooterDividersEnabled(false);
            footerView.setVisibility(View.VISIBLE);
        }

        setListAdapter(transactionListAdapter);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastShownItemIndex = firstVisibleItem + visibleItemCount;
                if (lastShownItemIndex == totalItemCount) {
                    transactionListAdapter.load();
                    if (transactionListAdapter.getCount() != 0) {
                        footerView.setVisibility(View.GONE);
                    } else {
                        footerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id ==  android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
