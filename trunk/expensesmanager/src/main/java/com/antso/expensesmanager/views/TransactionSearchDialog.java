package com.antso.expensesmanager.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.antso.expensesmanager.R;

public class TransactionSearchDialog extends Dialog {

    public interface OnDialogDismissed {
        void onDismissed(Boolean confirm, String searchText);
    }

    private OnDialogDismissed dismissListener;

    public TransactionSearchDialog(Context context, OnDialogDismissed dismissListener) {
        super(context);
        this.dismissListener = dismissListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.transaction_search_dialog);

        setTitle(R.string.title_search_transaction_dialog);
        final EditText text = (EditText) findViewById(R.id.searchDescription);
        Button search = (Button) findViewById(R.id.searchConfirm);
        Button cancel = (Button) findViewById(R.id.searchCancel);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!text.getText().toString().isEmpty()) {
                    dismissListener.onDismissed(true, text.getText().toString());
                } else {
                    dismissListener.onDismissed(false, null);
                }
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissListener.onDismissed(false, null);
                dismiss();
            }
        });

    }
}