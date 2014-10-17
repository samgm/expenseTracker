package com.antso.expensesmanager.budgets;

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
import android.widget.Toast;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.transactions.TransactionListActivity;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.views.BudgetChooseDialog;

public class BudgetListFragment extends ListFragment {

    private BudgetListAdapter budgetListAdapter = null;

    public BudgetListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (budgetListAdapter == null) {
            budgetListAdapter = new BudgetListAdapter(getActivity().getApplicationContext(),
                    BudgetManager.BUDGET_MANAGER());
            setListAdapter(budgetListAdapter);
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Object item = getListView().getItemAtPosition(position);
        if (item != null) {
            BudgetManager.BudgetInfo budgetInfo = (BudgetManager.BudgetInfo) item;
            Budget selectedBudget =  budgetInfo.budget;
            Intent intent = new Intent(getActivity(), TransactionListActivity.class);

            Bundle params = new Bundle();
            params.putString(IntentParamNames.BUDGET_ID, selectedBudget.getId());
            intent.putExtras(params);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clearHeader();
        menu.add(Constants.BUDGET_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 0, getText(R.string.action_budget_edit));
        menu.add(Constants.BUDGET_LIST_CONTEXT_MENU_GROUP_ID, v.getId(), 1, getText(R.string.action_budget_delete));
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
        final Budget budget = budgetInfo.budget;
        if (budget == null) {
            return true;
        }

        if (item.getTitle() == getText(R.string.action_budget_edit)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), BudgetEntryActivity.class);
            intent.putExtra(IntentParamNames.BUDGET_ID, budget.getId());
            getActivity().startActivityForResult(intent, Constants.BUDGET_EDIT_REQUEST_CODE);
        } else if(item.getTitle() == getText(R.string.action_budget_delete)) {
            if(BudgetManager.BUDGET_MANAGER().size() <= 1) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_error_dialog)
                        .setMessage(R.string.error_cannot_delete_last_budget)
                        .setNeutralButton(R.string.got_it, null)
                        .create();
                dialog.show();
                return true;
            }
            //choose budget to reassign
            BudgetChooseDialog dialog = new BudgetChooseDialog(
                    R.string.title_budget_chooser_dialog,
                    R.string.message_budget_chooser_dialog,
                    getActivity(),
                    new BudgetChooseDialog.OnDialogDismissed() {
                        @Override
                        public void onDismissed(boolean confirm, String selectedBudgetId) {
                            if (confirm) {
                                new DeleteBudgetAsyncTask(getActivity(), R.string.working,
                                        budget, selectedBudgetId).execute();
                            }
                        }
                    }, budget);
            dialog.show();
            return true;

        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_budget_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.account_menu_group, false);
        menu.setGroupVisible(R.id.budget_menu_group, true);
        menu.setGroupVisible(R.id.transaction_menu_group, false);
        menu.setGroupVisible(R.id.default_menu_group, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_budget_add) {
            Intent intent = new Intent(getActivity().getApplicationContext(), BudgetEntryActivity.class);
            startActivityForResult(intent, Constants.BUDGET_ENTRY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.BUDGET_ENTRY_REQUEST_CODE ||
                requestCode == Constants.BUDGET_EDIT_REQUEST_CODE) {
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
        budgetListAdapter.onDestroy();
        budgetListAdapter = null;
    }
}
