package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.ParcelableAccount;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionListActivity;
import com.antso.expensesmanager.utils.Constants;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountListFragment extends ListFragment {

    private View footerView;

    private AccountListAdapter accountListAdapter = null;
    private DatabaseHelper dbHelper = null;

    public AccountListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);

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
                AccountManager.ACCOUNT_MANAGER
                        .addAccount(account, dbHelper.getTransactions(account.getId()));
            }
            accountListAdapter = new AccountListAdapter(getActivity().getApplicationContext(),
                    AccountManager.ACCOUNT_MANAGER);

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
        Object item = getListView().getItemAtPosition(position);
        if (item != null) {
            AccountManager.AccountInfo accountInfo = (AccountManager.AccountInfo) item;
            Account selectedAccount =  accountInfo.account;
            Intent intent = new Intent(getActivity(), TransactionListActivity.class);

            Bundle params = new Bundle();
            params.putString("AccountId", selectedAccount.getId());
            intent.putExtras(params);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Action");   // Context-menu title
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, "Edit");
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, "Delete");    // Add element "Delete"
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() != Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID) {
            return false;
        }

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        AccountManager.AccountInfo accountInfo = (AccountManager.AccountInfo) accountListAdapter.getItem(index);
        Account account = accountInfo.account;
        if (account == null) {
            return true;
        }

        if (item.getTitle() == "Edit") {
            Toast.makeText(getActivity(), "Edit not supported", Toast.LENGTH_LONG).show();
        } else if(item.getTitle() == "Delete") {
            accountListAdapter.del(index);
            dbHelper.deleteAccount(account.getId());
            Toast.makeText(getActivity(), account.getName() + " Deleted", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_account_add) {
            Intent intent = new Intent(getActivity().getApplicationContext(), AccountEntryActivity.class);
            startActivityForResult(intent, Constants.ACCOUNT_ENTRY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACCOUNT_ENTRY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                final ParcelableAccount pAccount = data.getParcelableExtra("account");
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        accountListAdapter.add(pAccount.getAccount());
                    }
                });
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Do Nothing
            }
        }
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
