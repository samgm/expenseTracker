package com.antso.expensesmanager.transactions;

import android.app.Activity;
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
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.TransactionSearchDialog;

import java.util.Observable;
import java.util.Observer;

public class ExpensesListFragment extends ListFragment {

    private View footerView;
    private boolean searching = false;

    private ExpensesTransactionListAdapter transactionListAdapter = null;

    public ExpensesListFragment() {
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
            transactionListAdapter = new ExpensesTransactionListAdapter(
                    getActivity().getApplicationContext());

            if (footerView != null && transactionListAdapter.getCount() == 0) {
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.expenses_list_footer_text);
                textView.setTextColor(MaterialColours.GREY_500);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);
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
            startEditTransactionActivity(transaction);
        }
    }

    private void startEditTransactionActivity(Transaction transaction) {
        Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
        intent.putExtra(IntentParamNames.TRANSACTION_ID, transaction.getId());
        getActivity().startActivityForResult(intent, Constants.EXPENSE_TRANSACTION_EDIT_REQUEST_CODE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clearHeader();
        menu.add(Constants.EXPENSE_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0,
                getText(R.string.action_transaction_edit));
        menu.add(Constants.EXPENSE_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1,
                getText(R.string.action_transaction_delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId() != Constants.EXPENSE_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID) {
            return false;
        }

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Transaction transaction = (Transaction) transactionListAdapter.getItem(index);
        if (transaction == null) {
            return true;
        }

        if (item.getTitle() == getText(R.string.action_transaction_edit)) {
            startEditTransactionActivity(transaction);
        } else if(item.getTitle() == getText(R.string.action_transaction_delete)) {
            TransactionManager.TRANSACTION_MANAGER().removeTransaction(transaction);
            Toast.makeText(getActivity(), transaction.getDescription() +
                    getText(R.string.message_transaction_deleted), Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
        if (id == R.id.action_transaction_add) {
            Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
            intent.putExtra(IntentParamNames.TRANSACTION_DIRECTION, TransactionDirection.Out.getIntValue());
            getActivity().startActivityForResult(intent, Constants.EXPENSE_TRANSACTION_ENTRY_REQUEST_CODE);
            return true;
        }

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
        if (requestCode == Constants.EXPENSE_TRANSACTION_ENTRY_REQUEST_CODE ||
                requestCode == Constants.EXPENSE_TRANSACTION_EDIT_REQUEST_CODE) {
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
}
