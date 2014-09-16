package com.antso.expensesmanager.transactions;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.persistence.DatabaseHelper;

import java.util.Collection;

// Used to show a list of activities, for example when clicking on an account
//this is used to show the list of all the activity related to that account

public class TransactionListActivity extends ListActivity {

    // Add a ToDoItem Request Code
    private static final int ADD_TODO_ITEM_REQUEST = 0;
    TransactionListAdapter2 transactionListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle params = getIntent().getExtras();
        String accountId = params.getString("AccountId");

        AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER.getAccountInfo(accountId);
        transactionListAdapter = new TransactionListAdapter2(getApplicationContext(), accountInfo.transactions);
        setListAdapter(transactionListAdapter);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount)
            {
                int lastShownItemIndex = firstVisibleItem + visibleItemCount;

                //is the bottom item visible & not loading more already ? Load more !
                if((lastShownItemIndex == totalItemCount) /*&& !(loadingMore)*/) {
//                        Thread thread =  new Thread(null, loadMoreListItems);
//                        thread.start();
                    transactionListAdapter.loadMore();
                }
            }
        });
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
