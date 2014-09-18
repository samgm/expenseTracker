package com.antso.expensesmanager.transactions;

import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
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
import com.antso.expensesmanager.entities.ParcelableTransaction;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.Constants;

public class TransfersListFragment extends ListFragment {

    private View footerView;
    private TransactionListAdapter transactionListAdapter = null;

    public TransfersListFragment() {
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

        if (transactionListAdapter == null) {
            transactionListAdapter = new TransactionListAdapter(
                    getActivity().getApplicationContext(),
                    TransactionManager.TRANSACTION_MANAGER
                            .getTransactions(TransactionType.Transfer));

            if (footerView != null &&
                    TransactionManager.TRANSACTION_MANAGER
                            .getTransactions(TransactionType.Transfer).isEmpty()) {
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.transfers_list_footer_text);
                textView.setTextColor(Color.GRAY);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);
            }

            setListAdapter(transactionListAdapter);
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Action");   // Context-menu title
        menu.add(Constants.TRANSFER_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, "Edit");
        menu.add(Constants.TRANSFER_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId() != Constants.TRANSFER_TRANSACTION_LIST_CONTEXT_MENU_GROUP_ID) {
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
            transactionListAdapter.del(index);
            TransactionManager.TRANSACTION_MANAGER.removeTransaction(transaction);
            Toast.makeText(getActivity(), transaction.getDescription() + " Deleted", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_transaction_add) {
            Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
            intent.putExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
            intent.putExtra("transaction_type", TransactionType.Transfer.getIntValue());
            startActivityForResult(intent, Constants.TRANSFER_TRANSACTION_ENTRY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.TRANSFER_TRANSACTION_ENTRY_REQUEST_CODE) {
            if(resultCode == getActivity().RESULT_OK){
                final ParcelableTransaction pTransactionOut = data.getParcelableExtra("transaction_out");
                final ParcelableTransaction pTransactionIn = data.getParcelableExtra("transaction_in");

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        transactionListAdapter.add(pTransactionOut.getTransaction());
                        transactionListAdapter.add(pTransactionIn.getTransaction());
                    }
                });
            }
            if (resultCode == getActivity().RESULT_CANCELED) {
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
