package com.antso.expensesmanager.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.adapters.AccountSpinnerAdapter;
import com.antso.expensesmanager.entities.Account;

public class SpinnerChooserDialog implements DialogInterface.OnClickListener{
    private AlertDialog dialog;
    private OnDialogDismissed dismissListener;
    private Spinner spinner;

    public interface OnDialogDismissed {
        void onDismissed(boolean confirm, String selectedValue);
    }

    public SpinnerChooserDialog(int title, int message, Context context, OnDialogDismissed listener,
                                String[] values) {
        this.dismissListener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.chooser_spinner_dialog, null);

        final TextView msg = (TextView) view.findViewById(R.id.chooserSpinnerMessage);
        spinner = (Spinner) view.findViewById(R.id.chooserSpinner);
        msg.setText(message);
        spinner.setAdapter(new ArrayAdapter<Object>(context, R.layout.text_spinner_item, values));

        dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(R.string.cancel, this)
                .setPositiveButton(R.string.import_this, this)
                .create();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                dismissListener.onDismissed(true, spinner.getSelectedItem().toString());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dismissListener.onDismissed(false, null);
                break;
        }
    }
}
