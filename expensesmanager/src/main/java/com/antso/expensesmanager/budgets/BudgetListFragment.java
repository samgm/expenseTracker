package com.antso.expensesmanager.budgets;

import android.app.ListFragment;
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
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.Constants;

import java.util.Collection;

public class BudgetListFragment extends ListFragment {

    private View footerView;

    private BudgetListAdapter budgetListAdapter = null;

    public BudgetListFragment() {
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

        if (budgetListAdapter == null) {
            budgetListAdapter = new BudgetListAdapter(getActivity().getApplicationContext(),
                    BudgetManager.BUDGET_MANAGER);

            if (footerView != null && BudgetManager.BUDGET_MANAGER.getBudgetInfo().isEmpty()) {
                TextView textView = (TextView) footerView.findViewById(R.id.list_footer_message);
                textView.setText(R.string.budgets_list_footer_text);
                textView.setTextColor(Color.GRAY);

                getListView().addFooterView(footerView);
                getListView().setFooterDividersEnabled(true);
            }

            setListAdapter(budgetListAdapter);
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Object item = getListView().getItemAtPosition(position);
        if (item != null) {
            //TODO anything to do here
//            AccountManager.AccountInfo accountInfo = (AccountManager.AccountInfo) item;
//            Account selectedAccount =  accountInfo.account;
//            Intent intent = new Intent(getActivity(), TransactionListActivity.class);
//
//            Bundle params = new Bundle();
//            params.putString("AccountId", selectedAccount.getId());
//            intent.putExtras(params);
//            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Action");   // Context-menu title
        menu.add(Constants.BUDGET_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, "Edit");
        menu.add(Constants.BUDGET_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, "Delete");    // Add element "Delete"
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() != Constants.BUDGET_LIST_CONTEXT_MENU_GROUP_ID) {
            return false;
        }

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        BudgetManager.BudgetInfo budgetInfo = (BudgetManager.BudgetInfo) budgetListAdapter.getItem(index);
        Budget budget = budgetInfo.budget;
        if (budget == null) {
            return true;
        }

        if (item.getTitle() == "Edit") {
            Toast.makeText(getActivity(), "Edit not supported", Toast.LENGTH_LONG).show();
        } else if(item.getTitle() == "Delete") {
            Toast.makeText(getActivity(), "Delete not supported", Toast.LENGTH_LONG).show();
            //TODO support budget delete
//            budgetListAdapter.del(index);
//            Toast.makeText(getActivity(), account.getName() + " Deleted", Toast.LENGTH_LONG).show();
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
        if (id == R.id.action_budget_add) {
            //TODO support budget add
//            Intent intent = new Intent(getActivity().getApplicationContext(), AccountEntryActivity.class);
//            startActivityForResult(intent, Constants.ACCOUNT_ENTRY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == Constants.ACCOUNT_ENTRY_REQUEST_CODE) {
//            if(resultCode == Activity.RESULT_OK){
//                final ParcelableAccount pAccount = data.getParcelableExtra("account");
//                getActivity().runOnUiThread(new Runnable() {
//                    public void run() {
//                        budgetListAdapter.add(pAccount.getAccount());
//                    }
//                });
//            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Do Nothing
//            }
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        budgetListAdapter = null;
    }
}
