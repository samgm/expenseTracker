package com.antso.expenses.views_helpers;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.views.CircleSectorView;

public class TransactionLayout {
    private final Activity parentActivity;

    private CircleSectorView color;
    private LinearLayout secondaryAccountLayout;
    private TextView accountLabel;
    private TextView secondaryAccountLabel;
    private LinearLayout recurrentDetails;
    private LinearLayout feeDetails;
    private CheckBox recurrent;
    private CheckBox hasFee;

    public TransactionLayout(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int colorId, int secondaryAccountLayoutId,
                           int accountLabelId, int secondaryAccountLabelId,
                           int recurrentCheckboxId, int recurrentDetailsLayoutId,
                           int hasFeeCheckboxId, int feeDetailsLayoutId) {
        color = (CircleSectorView)parentActivity.findViewById(colorId);
        secondaryAccountLayout = (LinearLayout) parentActivity.findViewById(secondaryAccountLayoutId);
        secondaryAccountLabel = (TextView) parentActivity.findViewById(secondaryAccountLabelId);
        accountLabel = (TextView) parentActivity.findViewById(accountLabelId);
        recurrent = (CheckBox) parentActivity.findViewById(recurrentCheckboxId);
        recurrentDetails = (LinearLayout) parentActivity.findViewById(recurrentDetailsLayoutId);
        hasFee = (CheckBox) parentActivity.findViewById(hasFeeCheckboxId);
        feeDetails = (LinearLayout) parentActivity.findViewById(feeDetailsLayoutId);

        recurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                recurrentDetails.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        hasFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                feeDetails.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
            }
        });
    }

    public boolean isRecurrent() {
        return recurrent.isChecked();
    }

    public boolean hasFee() {
        return hasFee.isChecked();
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
                feeDetails.setVisibility(View.VISIBLE);
                hasFee.setVisibility(View.VISIBLE);
                break;
            case Single:
            case Undef:
                accountLabel.setText(R.string.account_label);
                feeDetails.setVisibility(View.GONE);
                hasFee.setVisibility(View.GONE);
                break;
        }

        if (transaction.getRecurrent()) {
            recurrent.setChecked(true);
            recurrentDetails.setVisibility(View.VISIBLE);
        } else {
            recurrent.setChecked(false);
            recurrentDetails.setVisibility(View.GONE);
        }

        if (transaction.hasFee()) {
            hasFee.setChecked(true);
            feeDetails.setVisibility(View.VISIBLE);
        } else {
            hasFee.setChecked(false);
            feeDetails.setVisibility(View.GONE);
        }
    }
}
