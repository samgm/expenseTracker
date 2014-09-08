package com.antso.expensesmanager.transactions;

import android.app.ListActivity;
import android.os.Bundle;

import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.persistence.DatabaseHelper;

import java.util.Collection;

// Used to show a list of activities, for example when clicking on an account
//this is used to show the list of all the activity related to that account

public class TransactionListActivity extends ListActivity {

    // Add a ToDoItem Request Code
    private static final int ADD_TODO_ITEM_REQUEST = 0;
    TransactionListAdapter transactionListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle params = getIntent().getExtras();
        String accountId = params.getString("AccountId");

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Collection<Transaction> transactions = dbHelper.getTransactions(accountId);

        transactionListAdapter = new TransactionListAdapter(getApplicationContext(), transactions);
        setListAdapter(transactionListAdapter);
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
