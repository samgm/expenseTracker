package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.transactions.TransactionListActivity;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.AccountChooserDialog;

public class AccountListFragment extends ListFragment {

    private View footerView;

    private AccountListAdapter accountListAdapter = null;

    public AccountListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);

        footerView = inflater.inflate(R.layout.list_footer, null, false);
        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (accountListAdapter == null) {
            accountListAdapter = new AccountListAdapter(getActivity().getApplicationContext(),
                    AccountManager.ACCOUNT_MANAGER());

            if (footerView != null && AccountManager.ACCOUNT_MANAGER().size() == 0) {
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
            params.putString(IntentParamNames.ACCOUNT_ID, selectedAccount.getId());
            intent.putExtras(params);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clearHeader();
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, getText(R.string.action_account_edit));
        menu.add(Constants.ACCOUNT_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, getText(R.string.action_account_delete));
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
        final Account account = accountInfo.account;
        if (account == null) {
            return true;
        }

        if (item.getTitle() == getText(R.string.action_account_edit)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), AccountEntryActivity.class);
            intent.putExtra(IntentParamNames.ACCOUNT_ID, account.getId());
            getActivity().startActivityForResult(intent, Constants.ACCOUNT_EDIT_REQUEST_CODE);
        } else if(item.getTitle() == getText(R.string.action_account_delete)) {
            if(AccountManager.ACCOUNT_MANAGER().size() <= 1) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_error_dialog)
                        .setMessage(R.string.error_cannot_delete_last_account)
                        .setNeutralButton(R.string.got_it, null)
                        .create();
                dialog.show();
                return true;
            }

            //choose account to reassign or del cascade
            AccountChooserDialog dialog = new AccountChooserDialog(
                    R.string.title_account_chooser_dialog,
                    R.string.message_account_chooser_dialog,
                    getActivity(),
                    new AccountChooserDialog.OnDialogDismissed() {
                        @Override
                        public void onDismissed(boolean confirm, boolean move, String selectedAccountId) {
                            if (confirm) {
                                if (move) {
                                    TransactionManager.TRANSACTION_MANAGER()
                                            .replaceAccount(account.getId(), selectedAccountId);
                                } else {
                                    TransactionManager.TRANSACTION_MANAGER()
                                            .removeTransactionByAccount(account.getId());
                                }
                                AccountManager.ACCOUNT_MANAGER().removeAccount(account);
                                accountListAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), account.getName() +
                                        getText(R.string.message_account_deleted),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }, account);
            dialog.show();
            return true;
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.account_menu_group, true);
        menu.setGroupVisible(R.id.budget_menu_group, false);
        menu.setGroupVisible(R.id.transaction_menu_group, false);
        menu.setGroupVisible(R.id.default_menu_group, false);

        super.onPrepareOptionsMenu(menu);
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
        if (requestCode == Constants.ACCOUNT_ENTRY_REQUEST_CODE ||
                requestCode == Constants.ACCOUNT_EDIT_REQUEST_CODE) {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accountListAdapter = null;
    }

}
