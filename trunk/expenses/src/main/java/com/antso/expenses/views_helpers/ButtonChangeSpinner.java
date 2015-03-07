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
    private ImageButton button;

    public ButtonChangeSpinner(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int spinnerId, int buttonId, SpinnerAdapter adapter) {
        this.spinner = (Spinner) parentActivity.findViewById(spinnerId);
        this.spinner.setAdapter(adapter);
        this.button = (ImageButton) parentActivity.findViewById(buttonId);
        this.button.setOnClickListener(onButtonClick());
    }

    public void setSelection(int i) {
        spinner.setSelection(i);
    }

    public Object getSelectedItem() {
        return spinner.getSelectedItem();
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = spinner.getSelectedItemPosition();
                spinner.setSelection((index + 1) % spinner.getAdapter().getCount());
            }
        };
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
    }

}
