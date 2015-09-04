package com.antso.expenses.budgets;

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

import com.antso.expenses.R;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.transactions.TransactionListActivity;
import com.antso.expenses.utils.Constants;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.views.BudgetChooseDialog;
import com.antso.expenses.views.TouchInterceptor;

public class BudgetListFragment extends ListFragment {
    private BudgetListAdapter budgetListAdapter = null;
    private boolean showMenu = false;

    public BudgetListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_list_host_fragment, container, false);

        FloatingActionButton myFab = (FloatingActionButton)view.findViewById(R.id.addFloatingButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), BudgetEntryActivity.class);
                startActivityForResult(intent, Constants.BUDGET_ENTRY_REQUEST_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (budgetListAdapter == null) {
            budgetListAdapter = new BudgetListAdapter(getActivity().getApplicationContext(),
                    BudgetManager.BUDGET_MANAGER());
            budgetListAdapter.setOnSelectionChangeHandler(new BudgetListAdapter.OnSelectionChanged() {
                @Override
                public void onSelectionChanged(int selectedItemIndex) {
                    showMenu = selectedItemIndex != -1;
                    getActivity().invalidateOptionsMenu();
                }
            });
            setListAdapter(budgetListAdapter);
        }

        TouchInterceptor mList = (TouchInterceptor) getListView();
        mList.setDropListener(new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                BudgetManager.BUDGET_MANAGER().sortBudgetInfo(from, to);
                budgetListAdapter.notifyDataSetChanged();
            }
        }, R.dimen.budget_item_height);

        registerForContextMenu(mList);
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

    private void editBudget(Budget budget) {
        Intent intent = new Intent(getActivity().getApplicationContext(), BudgetEntryActivity.class);
        intent.putExtra(IntentParamNames.BUDGET_ID, budget.getId());
        getActivity().startActivityForResult(intent, Constants.BUDGET_EDIT_REQUEST_CODE);
    }

    private boolean deleteBudget(final Budget budget) {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_budget_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.account_menu_group, false);
        menu.setGroupVisible(R.id.budget_menu_group, showMenu);
        menu.setGroupVisible(R.id.transaction_menu_group, false);
        menu.setGroupVisible(R.id.default_menu_group, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_budget_delete) {
            deleteBudget(budgetListAdapter.getSelectedBudget());
            budgetListAdapter.resetSelection();
            return true;
        }

        if (id == R.id.action_budget_edit) {
            editBudget(budgetListAdapter.getSelectedBudget());
            budgetListAdapter.resetSelection();
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
