package com.antso.expensesmanager;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.entities.TransactionDirection;
import com.antso.expensesmanager.entities.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by asolano on 5/4/2014.
 *
 * This class represent the view showing the list of all accounts
 * It uses list_fragment.xml layout, each element in the list uses account_item.xml layout
 */
public class AccountListFragment extends ListFragment {

    private final Context mContext;

    private View footerView;

    private AccountListAdapter accountListAdapter = null;
    private DatabaseHelper dbHelper = null;

    public AccountListFragment(final Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);

        //Create a footer view
        footerView = (LinearLayout) inflater.inflate(R.layout.account_list_footer, null, false);
        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getActivity().getApplicationContext());
        }

        if (accountListAdapter == null) {
            Collection<Account> accounts = dbHelper.getAccounts();
            for (Account account : accounts) {
                Collection<Transaction> transactions = dbHelper.getTransactions(account.getId());
                account.setTransactions(transactions);
            }
            accountListAdapter = new AccountListAdapter(mContext, accounts);

            if (footerView != null) {
                TextView textView = (TextView) footerView.findViewById(R.id.account_list_footer_message);
                textView.setText(R.string.account_list_footer_text);
                textView.setTextColor(Color.GRAY);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);

                footerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("AccountListFragment", "Account List footer clicked");

    //                // Attach Listener to FooterView. Implement onClick().
    //                Intent intent = new Intent(getApplicationContext(), AddToDoActivity.class);
    //                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);

                        Account account1 = new Account("ACC1", "Account1", BigDecimal.valueOf(1500.00), Color.rgb(0,0,255));
                        dbHelper.insertAccount(account1);
                        accountListAdapter.add(account1);

                        Account account2 = new Account("ACC2", "Account2", BigDecimal.valueOf(1600.00), Color.rgb(255,0,255));
                        dbHelper.insertAccount(account2);
                        accountListAdapter.add(account2);

                        Account account3 = new Account("ACC3", "Account3", BigDecimal.valueOf(1700.55), Color.rgb(125,125,255));
                        dbHelper.insertAccount(account3);
                        accountListAdapter.add(account3);
                    }
                });
            }

            setListAdapter(accountListAdapter);
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Account selectedAccount = (Account)getListView().getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), TransactionListActivity.class);

        Bundle params = new Bundle();
        params.putString("AccountId", selectedAccount.getId());
        intent.putExtras(params);
        startActivity(intent);

        //Toast.makeText(getActivity(), getListView().getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Action");   // Context-menu title
        menu.add(0, v.getId(), 0, "Edit");      // Add element "Edit"
        menu.add(0, v.getId(), 1, "Delete");    // Add element "Delete"
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Account account = (Account) accountListAdapter.getItem(index);
        if (account == null) {
            return false;
        }

        if (item.getTitle() == "Edit") {
            Toast.makeText(getActivity(), "Selected 'Edit' on item " + account.getName(), Toast.LENGTH_LONG).show();
        } else if(item.getTitle() == "Delete") {
            accountListAdapter.del(index);
            dbHelper.deleteAccount(account.getId());
            Toast.makeText(getActivity(), account.getName() + " Deleted", Toast.LENGTH_LONG).show();
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }

        accountListAdapter = null;
    }
}
