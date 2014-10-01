package com.antso.expensesmanager.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.utils.BudgetSpinnerAdapter;

public class BudgetChooseDialog implements DialogInterface.OnClickListener{
    private AlertDialog dialog;
    private OnDialogDismissed dismissListener;
    private Spinner spinner;

    public interface OnDialogDismissed {
        void onDismissed(boolean confirm, String selectedBudgetId);
    }

    public BudgetChooseDialog(int title, int message, Context context, OnDialogDismissed listener,
                              Budget budgetToDelete) {
        this.dismissListener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.chooser_spinner_dialog, null);

        final TextView msg = (TextView) view.findViewById(R.id.chooserSpinnerMessage);
        spinner = (Spinner) view.findViewById(R.id.chooserSpinner);
        msg.setText(message);
        spinner.setAdapter(BudgetSpinnerAdapter.create(context, R.layout.text_spinner_item,
                BudgetManager.BUDGET_MANAGER.getBudgets().toArray(new Budget[0]), budgetToDelete));

        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(R.string.cancel, this)
                .setPositiveButton(R.string.delete_and_move, this)
                .create();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                dismissListener.onDismissed(true, ((Budget)spinner.getSelectedItem()).getId());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dismissListener.onDismissed(false, null);
                break;
        }
    }
}