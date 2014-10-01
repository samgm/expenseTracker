package com.antso.expensesmanager.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.utils.AccountSpinnerAdapter;

public class AccountChooserDialog implements DialogInterface.OnClickListener{
    private AlertDialog dialog;
    private OnDialogDismissed dismissListener;
    private Spinner spinner;

    public interface OnDialogDismissed {
        void onDismissed(boolean confirm, boolean move, String selectedAccountId);
    }

    public AccountChooserDialog(int title, int message, Context context, OnDialogDismissed listener,
                                Account accountToDelete) {
        this.dismissListener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.chooser_spinner_dialog, null);

        final TextView msg = (TextView) view.findViewById(R.id.chooserSpinnerMessage);
        spinner = (Spinner) view.findViewById(R.id.chooserSpinner);
        msg.setText(message);
        spinner.setAdapter(AccountSpinnerAdapter.create(context, R.layout.text_spinner_item,
                AccountManager.ACCOUNT_MANAGER.getAccounts().toArray(new Account[0]), accountToDelete));

        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(R.string.cancel, this)
                .setPositiveButton(R.string.delete_all, this)
                .setNeutralButton(R.string.delete_and_move, this)
                .create();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                dismissListener.onDismissed(true, false, ((Account)spinner.getSelectedItem()).getId());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dismissListener.onDismissed(false, false, null);
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                dismissListener.onDismissed(true, true, ((Account)spinner.getSelectedItem()).getId());
                break;
        }
    }
}
