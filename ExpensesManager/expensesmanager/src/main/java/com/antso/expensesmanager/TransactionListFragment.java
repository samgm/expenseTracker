package com.antso.expensesmanager;

import android.app.ListFragment;
import android.content.Context;
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
public class TransactionListFragment extends ListFragment {

    private final Context mContext;

    private View footerView;

    private TransactionListAdapter transactionListAdapter = null;
    private DatabaseHelper dbHelper = null;

    public TransactionListFragment(final Context mContext) {
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
            Collection<Transaction> transactions = dbHelper.getTransactions();

            transactionListAdapter = new TransactionListAdapter(mContext, transactions);

            if (footerView != null) {
                TextView textView = (TextView) footerView.findViewById(R.id.transaction_list_footer_message);
                textView.setText(R.string.transaction_list_footer_text);
                textView.setTextColor(Color.GRAY);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);

                footerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("TransactionListFragment", "Transaction List footer clicked");

                        //                // Attach Listener to FooterView. Implement onClick().
                        //                Intent intent = new Intent(getApplicationContext(), AddToDoActivity.class);
                        //                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);

                        Transaction t1 = new Transaction("TR1", "Test Transaction 1",
                                TransactionDirection.In, TransactionType.Single, "ACC1", "BG1",
                                BigDecimal.valueOf(100.0), new DateTime(2014, 3, 5, 18, 05));
                        Transaction t2 = new Transaction("TR2", "Test Transaction 2",
                                TransactionDirection.Out, TransactionType.Single, "ACC1", "BG1",
                                BigDecimal.valueOf(59.5), new DateTime(2014, 3, 8, 14, 10));

                        Transaction t3 = new Transaction("TR3", "Test Transaction 3",
                                TransactionDirection.In, TransactionType.Single, "ACC2", "BG1",
                                BigDecimal.valueOf(52.89), new DateTime(2014, 4, 16, 13, 15));
                        Transaction t4 = new Transaction("TR4", "Test Transaction 4",
                                TransactionDirection.In, TransactionType.Single, "ACC2", "BG1",
                                BigDecimal.valueOf(12.23), new DateTime(2014, 5, 11, 16, 29));

                        dbHelper.insertTransactions(t1);
                        dbHelper.insertTransactions(t2);
                        dbHelper.insertTransactions(t3);
                        dbHelper.insertTransactions(t4);

                        transactionListAdapter.add(t1);
                        transactionListAdapter.add(t2);
                        transactionListAdapter.add(t3);
                        transactionListAdapter.add(t4);
                    }
                });
            }

            setListAdapter(transactionListAdapter);
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Toast.makeText(getActivity(), getListView().getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
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
//        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//        int index = info.position;
//
//        Account account = (Account) accountListAdapter.getItem(index);
//        if (account == null) {
//            return false;
//        }
//
//        if (item.getTitle() == "Edit") {
//            Toast.makeText(getActivity(), "Selected 'Edit' on item " + account.getName(), Toast.LENGTH_LONG).show();
//        } else if(item.getTitle() == "Delete") {
//            accountListAdapter.del(index);
//            dbHelper.deleteAccount(account.getId());
//            Toast.makeText(getActivity(), account.getName() + " Deleted", Toast.LENGTH_LONG).show();
//        } else {
//            return false;
//        }

        return true;
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
