package com.antso.expenses.accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.antso.expenses.R;
import com.antso.expenses.entities.Account;
import com.antso.expenses.transactions.TransactionListActivity;
import com.antso.expenses.utils.Constants;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.AccountChooserDialog;
import com.antso.expenses.views.TouchInterceptor;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountListFragment extends ListFragment {
    private AccountListAdapter accountListAdapter = null;
    private boolean showMenu = false;

    public AccountListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_list_host_fragment, container, false);

        FloatingActionButton myFab = (FloatingActionButton)view.findViewById(R.id.addFloatingButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AccountEntryActivity.class);
                startActivityForResult(intent, Constants.ACCOUNT_ENTRY_REQUEST_CODE);
            }
        });

        Toolbar summaryBar = (Toolbar) view.findViewById(R.id.accountListToolbar);
        Collection<AccountManager.AccountInfo> accounts = AccountManager.ACCOUNT_MANAGER().getAccountInfo();
        BigDecimal totMonthBalance = BigDecimal.ZERO;
        BigDecimal totMonthIn = BigDecimal.ZERO;
        BigDecimal totMonthOut = BigDecimal.ZERO;
        BigDecimal totBalance = BigDecimal.ZERO;
        for (AccountManager.AccountInfo account : accounts) {
            totBalance = totBalance.add(account.balance);
            totMonthBalance = totMonthBalance.add(account.monthBalance);
            totMonthIn = totMonthIn.add(account.monthIn);
            totMonthOut = totMonthOut.add(account.monthOut);
        }

        TextView total = (TextView) summaryBar.findViewById(R.id.accountSummaryGrandTotalBalance);
        total.setText(Utils.getCurrencyString(getActivity()) + " " +
                totBalance.setScale(2, BigDecimal.ROUND_UP).toPlainString());
        TextView in = (TextView) summaryBar.findViewById(R.id.accountSummaryMonthIn);
        in.setText(Utils.getCurrencyString(getActivity()) + " " +
                totMonthIn.setScale(2, BigDecimal.ROUND_UP).toPlainString());
        TextView out = (TextView) summaryBar.findViewById(R.id.accountSummaryMonthOut);
        out.setText(Utils.getCurrencyString(getActivity()) + " " +
                totMonthOut.setScale(2, BigDecimal.ROUND_UP).toPlainString());
        TextView balance = (TextView) summaryBar.findViewById(R.id.accountSummaryMonthBalance);
        balance.setText(Utils.getCurrencyString(getActivity()) + " " +
                totMonthBalance.setScale(2, BigDecimal.ROUND_UP).toPlainString());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (accountListAdapter == null) {
            accountListAdapter = new AccountListAdapter(getActivity().getApplicationContext(),
                    AccountManager.ACCOUNT_MANAGER());
            accountListAdapter.setOnSelectionChangeHandler(new AccountListAdapter.OnSelectionChanged() {
                @Override
                public void onSelectionChanged(int selectedItemIndex) {
                    showMenu = selectedItemIndex != -1;
                    getActivity().invalidateOptionsMenu();
                }
            });
            setListAdapter(accountListAdapter);
        }

        TouchInterceptor mList = (TouchInterceptor) getListView();
        mList.setDropListener(new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                AccountManager.ACCOUNT_MANAGER().sortAccountInfo(from, to);
                accountListAdapter.notifyDataSetChanged();
            }
        }, R.dimen.account_item_height);

        registerForContextMenu(mList);
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

    private boolean deleteAccount(final Account account) {
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
                            new DeleteAccountAsyncTask(getActivity(), R.string.working,
                                    account, selectedAccountId).execute();
                        }
                    }
                }, account);
        dialog.show();
        return true;
    }

    private void editAccount(Account account) {
        Intent intent = new Intent(getActivity().getApplicationContext(), AccountEntryActivity.class);
        intent.putExtra(IntentParamNames.ACCOUNT_ID, account.getId());
        getActivity().startActivityForResult(intent, Constants.ACCOUNT_EDIT_REQUEST_CODE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.account_menu_group, showMenu);
        menu.setGroupVisible(R.id.budget_menu_group, false);
        menu.setGroupVisible(R.id.transaction_menu_group, false);
        menu.setGroupVisible(R.id.default_menu_group, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account_delete) {
            deleteAccount(accountListAdapter.getSelectedAccount());
            accountListAdapter.resetSelection();
            return true;
        }

        if (id == R.id.action_account_edit) {
            editAccount(accountListAdapter.getSelectedAccount());
            accountListAdapter.resetSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACCOUNT_ENTRY_REQUEST_CODE ||
                requestCode == Constants.ACCOUNT_EDIT_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                //Do Nothing
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
        accountListAdapter.onDestroy();
        accountListAdapter = null;
    }

}
