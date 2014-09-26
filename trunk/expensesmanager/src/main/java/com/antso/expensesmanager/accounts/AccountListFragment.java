package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.transactions.TransactionListActivity;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.MaterialColours;

public class AccountListFragment extends ListFragment {

    private View footerView;

    private AccountListAdapter accountListAdapter = null;

    public AccountListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);

        footerView = (LinearLayout) inflater.inflate(R.layout.list_footer, null, false);
        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (accountListAdapter == null) {
            accountListAdapter = new AccountListAdapter(getActivity().getApplicationContext(),
                    AccountManager.ACCOUNT_MANAGER);

            if (footerView != null && AccountManager.ACCOUNT_MANAGER.getAccountInfo().isEmpty()) {
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.accounts_list_footer_text);
                textView.setTextColor(MaterialColours.GREY_500);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);
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
            params.putString("account_id", selectedAccount.getId());
            intent.putExtras(params);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clearHeader();
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, "Edit");
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, "Delete");
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
            AccountManager.ACCOUNT_MANAGER.removeAccount(account);
            accountListAdapter.notifyDataSetChanged();
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        accountListAdapter.notifyDataSetChanged();
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
        accountListAdapter = null;
    }
}
