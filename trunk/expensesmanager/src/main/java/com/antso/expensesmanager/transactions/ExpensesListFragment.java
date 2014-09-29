package com.antso.expensesmanager.transactions;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.TransactionSearchDialog;

public class ExpensesListFragment extends ListFragment {

    private View footerView;
    private static Menu optionMenu;

    private ExpensesTransactionListAdapter transactionListAdapter = null;

    public ExpensesListFragment() {
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

        if (optionMenu != null) {
            MenuItem searchUndoItem = optionMenu.findItem(R.id.action_transaction_search_undo);
            searchUndoItem.setVisible(false);
            MenuItem searchItem = optionMenu.findItem(R.id.action_transaction_search);
            searchItem.setVisible(true);
        }

        if (transactionListAdapter == null) {
            transactionListAdapter = new ExpensesTransactionListAdapter(
                    getActivity().getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER);

            if (footerView != null &&
                    TransactionManager.TRANSACTION_MANAGER
                            .getTransactions(TransactionDirection.Out, true).isEmpty()) {
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.expenses_list_footer_text);
                textView.setTextColor(MaterialColours.GREY_500);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);

            }

            setListAdapter(transactionListAdapter);
        } else {
            transactionListAdapter.resetSearch();
            transactionListAdapter.notifyDataSetChanged();
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Object item = getListView().getItemAtPosition(position);
        if (item != null) {
            Toast.makeText(getActivity(), item.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clearHeader();
        menu.add(Constants.EXPENSE_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, "Edit");
        menu.add(Constants.EXPENSE_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, "Delete");
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

        if (item.getTitle() == "Edit") {
            Toast.makeText(getActivity(), "Edit not supported", Toast.LENGTH_LONG).show();
        } else if(item.getTitle() == "Delete") {
            TransactionManager.TRANSACTION_MANAGER.removeTransaction(transaction);
            transactionListAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), transaction.getDescription() + " Deleted", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);
        this.optionMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_transaction_add) {
            Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
            intent.putExtra("transaction_direction", TransactionDirection.Out.getIntValue());
            startActivityForResult(intent, Constants.EXPENSE_TRANSACTION_ENTRY_REQUEST_CODE);
            return true;
        }

        if (id == R.id.action_transaction_search) {
            final TransactionSearchDialog dialog = new TransactionSearchDialog(getActivity(),
                    new TransactionSearchDialog.OnDialogDismissed() {
                        @Override
                        public void onDismissed(Boolean confirm, String searchText) {
                            if (confirm) {
                                transactionListAdapter.search(searchText);
                                transactionListAdapter.notifyDataSetChanged();
                                MenuItem searchItem = optionMenu.findItem(R.id.action_transaction_search);
                                searchItem.setVisible(false);
                                MenuItem undoSearchItem = optionMenu.findItem(R.id.action_transaction_search_undo);
                                undoSearchItem.setVisible(true);
                            }
                        }
                    });
            dialog.show();
            return true;
        }

        if (id == R.id.action_transaction_search_undo) {
            item.setVisible(false);
            MenuItem searchItem = optionMenu.findItem(R.id.action_transaction_search);
            searchItem.setVisible(true);
            transactionListAdapter.resetSearch();
            transactionListAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.EXPENSE_TRANSACTION_ENTRY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        transactionListAdapter.notifyDataSetChanged();
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
        transactionListAdapter = null;
    }
}
