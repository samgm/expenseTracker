package com.antso.expenses.transactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.utils.Constants;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.views.TransactionSearchDialog;

public class RevenuesListFragment extends ListFragment implements HandlingFooterFragment {

    private View footerView;
	private boolean searching = false;

    private RevenuesTransactionListAdapter transactionListAdapter = null;

    public RevenuesListFragment() {
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
        searching = false;

        if (transactionListAdapter == null) {
            transactionListAdapter = new RevenuesTransactionListAdapter(
                    getActivity().getApplicationContext(), this);

            if (footerView != null) {
                footerView.setVisibility(View.GONE);
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.revenues_list_footer_text);
                textView.setTextColor(MaterialColours.GREY_500);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(false);
            }

            setListAdapter(transactionListAdapter);
        } else {
            transactionListAdapter.resetSearch();
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Transaction transaction = (Transaction)getListView().getItemAtPosition(position);
        if (transaction != null) {
            AccountManager.AccountInfo accountInfo =
                    AccountManager.ACCOUNT_MANAGER().getAccountInfo(transaction.getAccountId());
            if (accountInfo.account.isArchived()) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_error_dialog)
                        .setMessage(R.string.error_cannot_open_transactions_from_archived_account)
                        .setNeutralButton(R.string.got_it, null)
                        .create();
                dialog.show();
                return;
            }

            startEditTransactionActivity(transaction);
        }

    }

    private void startEditTransactionActivity(Transaction transaction) {
        Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
        intent.putExtra(IntentParamNames.TRANSACTION_ID, transaction.getId());
        getActivity().startActivityForResult(intent, Constants.REVENUE_TRANSACTION_EDIT_REQUEST_CODE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.i("EXPENSES MENU", "PrepareOptionsMenu for " + this);

        menu.setGroupVisible(R.id.account_menu_group, false);
        menu.setGroupVisible(R.id.budget_menu_group, false);
        menu.setGroupVisible(R.id.transaction_menu_group, true);
        menu.setGroupVisible(R.id.default_menu_group, false);

        MenuItem searchUndoItem = menu.findItem(R.id.action_transaction_search_undo);
        MenuItem searchItem = menu.findItem(R.id.action_transaction_search);
        if (searching) {
            searchUndoItem.setVisible(true);
            searchItem.setVisible(false);
        } else {
            searchUndoItem.setVisible(false);
            searchItem.setVisible(true);
        }


        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_transaction_search) {
            final TransactionSearchDialog dialog = new TransactionSearchDialog(getActivity(),
                    new TransactionSearchDialog.OnDialogDismissed() {
                        @Override
                        public void onDismissed(Boolean confirm, String searchText) {
                            if (confirm) {
                                transactionListAdapter.search(searchText);
                                searching = true;
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                    });
            dialog.show();
            return true;
        }

        if (id == R.id.action_transaction_search_undo) {
            transactionListAdapter.resetSearch();
            searching = false;
            getActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REVENUE_TRANSACTION_ENTRY_REQUEST_CODE ||
                requestCode == Constants.REVENUE_TRANSACTION_EDIT_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                transactionListAdapter.resetSearch();
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
        transactionListAdapter.onDestroy();
        transactionListAdapter = null;
    }

    @Override
    public void showFooter() {
        footerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFooter() {
        footerView.setVisibility(View.GONE);
    }
}
