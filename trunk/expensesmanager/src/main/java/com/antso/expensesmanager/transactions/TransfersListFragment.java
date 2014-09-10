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
import com.antso.expensesmanager.entities.TransactionDirection;
import com.antso.expensesmanager.entities.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.Constants;

public class TransfersListFragment extends ListFragment {

    private View footerView;

    private TransactionListAdapter transactionListAdapter = null;
    private DatabaseHelper dbHelper = null;

    public TransfersListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);

        footerView = (LinearLayout) inflater.inflate(R.layout.transaction_list_footer, null, false);
        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getActivity().getApplicationContext());
        }

        if (transactionListAdapter == null) {
            transactionListAdapter = new TransactionListAdapter(
                    getActivity().getApplicationContext(),
                    dbHelper.getTransactions(TransactionType.Transfer));

            if (footerView != null &&
                    dbHelper.getTransactions(TransactionType.Transfer).isEmpty()) {
                TextView textView = (TextView) footerView.findViewById(R.id.transaction_list_footer_message);
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
        menu.add(0, v.getId(), 0, "Edit");      // Add element "Edit"
        menu.add(0, v.getId(), 1, "Delete");    // Add element "Delete"
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Transaction transaction = (Transaction) transactionListAdapter.getItem(index);
        if (transaction == null) {
            return false;
        }

        if (item.getTitle() == "Edit") {
            Toast.makeText(getActivity(), "Edit not supported", Toast.LENGTH_LONG).show();
        } else if(item.getTitle() == "Delete")
        {
            //TODO review as required per transfer
            transactionListAdapter.del(index);
            dbHelper.deleteTransaction(transaction.getId());
            Toast.makeText(getActivity(), transaction.getDescription() + " Deleted", Toast.LENGTH_LONG).show();
        } else {
            return false;
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
            //TODO review as required per transfer
            intent.putExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
            startActivityForResult(intent, Constants.TRANSFER_TRANSACTION_ENTRY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.TRANSFER_TRANSACTION_ENTRY_REQUEST_CODE) {
            if(resultCode == getActivity().RESULT_OK){
                //TODO review as required per transfer

//                final ParcelableTransaction pTransaction = data.getParcelableExtra("transaction");
//                getActivity().runOnUiThread(new Runnable() {
//                    public void run() {
//                        transactionListAdapter.add(pTransaction.getTransaction());
//                    }
//                });
            }
            if (resultCode == getActivity().RESULT_CANCELED) {
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

        transactionListAdapter = null;
    }
}
