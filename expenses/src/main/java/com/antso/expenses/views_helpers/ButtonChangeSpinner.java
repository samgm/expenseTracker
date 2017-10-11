package com.antso.expenses.views_helpers;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class ButtonChangeSpinner {
    private final Activity parentActivity;

    private Spinner spinner;
    @SuppressWarnings("FieldCanBeLocal")

    public ButtonChangeSpinner(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int spinnerId, SpinnerAdapter adapter) {
        this.spinner = (Spinner) parentActivity.findViewById(spinnerId);
        this.spinner.setAdapter(adapter);
    }

    public void setSelection(int i) {
        spinner.setSelection(i);
    }

    public Object getSelectedItem() {
        return spinner.getSelectedItem();
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
    }

}
