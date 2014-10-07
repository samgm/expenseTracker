package com.antso.expensesmanager.transactions;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.utils.MaterialColours;

// Used to show a list of activities, for example when clicking on an account
//this is used to show the list of all the activity related to that account

public class TransactionListActivity extends ListActivity {
    private AbstractTransactionListAdapter transactionListAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        Bundle params = getIntent().getExtras();
        String accountId = params.getString(IntentParamNames.ACCOUNT_ID);
        String budgetId = params.getString(IntentParamNames.BUDGET_ID);

        if(accountId != null) {
            setTitle(R.string.title_accounts_transaction_list_activity);
            transactionListAdapter = new AccountTransactionListAdapter(getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER,
                    accountId);
        }
        if(budgetId != null) {
            setTitle(R.string.title_budgets_transaction_list_activity);
            transactionListAdapter = new BudgetTransactionListAdapter(getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER,
                    budgetId);
        }

        setListAdapter(transactionListAdapter);

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
                }
            }
        });

        LinearLayout footerView = (LinearLayout) getLayoutInflater().inflate(R.layout.list_footer, null, false);
        if (footerView != null && transactionListAdapter.getCount() == 0) {
            TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
            textView.setText(R.string.transaction_list_footer_text);
            textView.setTextColor(MaterialColours.GREY_500);

            getListView().addFooterView(footerView);
            getListView().setFooterDividersEnabled(true);
        }
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
