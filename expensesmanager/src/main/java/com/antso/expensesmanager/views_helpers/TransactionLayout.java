package com.antso.expensesmanager.views_helpers;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.CircleSectorView;

public class TransactionLayout {
    private final Activity parentActivity;

    private CircleSectorView color;
    private LinearLayout secondaryAccountLayout;
    private TextView accountLabel;
    private TextView secondaryAccountLabel;
    private LinearLayout recurrentDetails;
    private CheckBox recurrent;

    public TransactionLayout(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int colorId, int secondaryAccountLayoutId,
                           int accountLabelId, int secondaryAccountLabelId,
                           int recurrentCheckboxId, int recurrentDetailsLayoutId) {
        color = (CircleSectorView)parentActivity.findViewById(colorId);
        secondaryAccountLayout = (LinearLayout) parentActivity.findViewById(secondaryAccountLayoutId);
        secondaryAccountLabel = (TextView) parentActivity.findViewById(secondaryAccountLabelId);
        accountLabel = (TextView) parentActivity.findViewById(accountLabelId);
        recurrent = (CheckBox) parentActivity.findViewById(recurrentCheckboxId);
        recurrentDetails = (LinearLayout) parentActivity.findViewById(recurrentDetailsLayoutId);

        recurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                recurrentDetails.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
            }
        });
    }

    public boolean isRecurrent() {
        return recurrent.isChecked();
    }

    public void setTransaction(Transaction transaction) {
        switch (transaction.getDirection()) {
            case In:
                color.setColor(MaterialColours.GREEN_500);
                break;
            case Out:
                color.setColor(MaterialColours.RED_500);
                break;
            case Undef:
                break;
        }

        switch (transaction.getType()) {
            case Transfer:
                color.setColor(MaterialColours.YELLOW_500);
                secondaryAccountLayout.setVisibility(View.VISIBLE);
                secondaryAccountLabel.setVisibility(View.VISIBLE);
                accountLabel.setText(R.string.from_label);
                break;
            case Single:
            case Undef:
                accountLabel.setText(R.string.account_label);
                break;
        }

        if (transaction.getRecurrent()) {
            recurrent.setChecked(true);
            recurrentDetails.setVisibility(View.VISIBLE);
        } else {
            recurrent.setChecked(false);
            recurrentDetails.setVisibility(View.GONE);
        }
    }
}
