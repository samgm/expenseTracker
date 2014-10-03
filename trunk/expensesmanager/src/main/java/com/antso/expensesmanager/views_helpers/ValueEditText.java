package com.antso.expensesmanager.views_helpers;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import com.antso.expensesmanager.utils.Utils;

import java.math.BigDecimal;

public class ValueEditText {
    private final Activity parentActivity;

    private EditText valueEditText;
    private BigDecimal value;

    public ValueEditText(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int editViewId, BigDecimal value) {
        this.value = value;
        this.valueEditText = (EditText) parentActivity.findViewById(editViewId);
        this.valueEditText.setOnFocusChangeListener(onFocusChanged());
        this.valueEditText.setText(value.setScale(2).toString());
    }

    public void setValue(BigDecimal value) {
        this.value = value;
        this.valueEditText.setText(value.setScale(2).toString());
    }

    public BigDecimal getValue() {
        String valueStr = valueEditText.getText().toString();
        valueStr = Utils.washDecimalNumber(valueStr);
        value = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
        return this.value;
    }

    private View.OnFocusChangeListener onFocusChanged() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = valueEditText.getText().toString();
                    valueStr = Utils.washDecimalNumber(valueStr);
                    value = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
                    valueEditText.setText(value.toPlainString());
                }
            }
        };
    }

}
