package com.antso.expenses.views_helpers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

public class DateEditText {
    private final Activity parentActivity;

    private EditText dateEditText;
    private DateTime date;

    public DateEditText(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int editViewId, DateTime date) {
        this.date = date;
        this.dateEditText = (EditText) parentActivity.findViewById(editViewId);
        this.dateEditText.setOnClickListener(onClick());
        this.dateEditText.setOnFocusChangeListener(onFocusChanged());
        this.dateEditText.setText(Utils.formatDate(date));
    }

    public void setDate(DateTime date) {
        if (date.equals(Utils.DEFAULT_DATE)) {
            return;
        }

        this.date = date;
        this.dateEditText.setText(Utils.formatDate(date));
    }

    public DateTime getDate() {
        return this.date;
    }

    private View.OnFocusChangeListener onFocusChanged() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePicker = new DatePickerDialog(
                            parentActivity,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                    dateEditText.setText(Utils.formatDate(date));

                                    //hide keyboard on date selection as not required
                                    InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), 0);
                                }
                            }, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth()
                    );
                    datePicker.show();
                }
            }
        };
    }

    private View.OnClickListener onClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime now = Utils.now();
                DatePickerDialog datePicker = new DatePickerDialog(
                        parentActivity,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                dateEditText.setText(Utils.formatDate(date));

                                //hide keyboard on date selection as not required
                                InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), 0);
                            }
                        }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                );
                datePicker.show();
            }
        };
    }

}
