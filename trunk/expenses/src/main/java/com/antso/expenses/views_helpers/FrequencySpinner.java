package com.antso.expenses.views_helpers;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.antso.expenses.R;
import com.antso.expenses.adapters.IntegerArraySpinnerAdapter;
import com.antso.expenses.adapters.TimeUnitSpinnerAdapter;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.utils.Utils;

public class FrequencySpinner {
    private final Activity parentActivity;

    private Spinner unitSpinner;
    private Spinner valueSpinner;
    private TimeUnitSpinnerAdapter unitSpinnerAdapter;
    private IntegerArraySpinnerAdapter currentValueSpinnerAdapter;

    private IntegerArraySpinnerAdapter dayValuesAdapter;
    private IntegerArraySpinnerAdapter weekValuesAdapter;
    private IntegerArraySpinnerAdapter monthValuesAdapter;
    private IntegerArraySpinnerAdapter yearValuesAdapter;

    public FrequencySpinner(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void createView(int unitSpinnerViewId, int valueSpinnerViewId) {
        unitSpinnerAdapter = TimeUnitSpinnerAdapter.create(parentActivity,
                R.layout.text_spinner_item);
        dayValuesAdapter = IntegerArraySpinnerAdapter.create(parentActivity,
                    R.layout.text_spinner_item, Utils.DayValues);
        weekValuesAdapter = IntegerArraySpinnerAdapter.create(parentActivity,
                R.layout.text_spinner_item, Utils.WeekValues);
        monthValuesAdapter = IntegerArraySpinnerAdapter.create(parentActivity,
                R.layout.text_spinner_item, Utils.MonthValues);
        yearValuesAdapter = IntegerArraySpinnerAdapter.create(parentActivity,
                R.layout.text_spinner_item, Utils.YearValues);

        currentValueSpinnerAdapter = dayValuesAdapter;

        this.unitSpinner = (Spinner) parentActivity.findViewById(unitSpinnerViewId);
        this.valueSpinner = (Spinner) parentActivity.findViewById(valueSpinnerViewId);
        this.unitSpinner.setAdapter(unitSpinnerAdapter);
        this.unitSpinner.setOnItemSelectedListener(onUnitSpinnerSelected());
    }

    public int getValue() {
        return (Integer)valueSpinner.getSelectedItem();
    }

    public TimeUnit getUnit() {
        return (TimeUnit)unitSpinner.getSelectedItem();
    }

    private AdapterView.OnItemSelectedListener onUnitSpinnerSelected() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeUnit unit = TimeUnit.valueOf(position + 1);
                switch (unit) {
                    case Day:
                        currentValueSpinnerAdapter = dayValuesAdapter;
                        break;
                    case Week:
                        currentValueSpinnerAdapter = weekValuesAdapter;
                        break;
                    case Month:
                        currentValueSpinnerAdapter = monthValuesAdapter;
                        break;
                    case Year:
                        currentValueSpinnerAdapter = yearValuesAdapter;
                        break;
                    case Undef:
                    default:
                        break;
                }

                valueSpinner.setAdapter(currentValueSpinnerAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    public void setUnit(TimeUnit frequencyUnit) {
        unitSpinner.setSelection(unitSpinnerAdapter.getIndexByValue(frequencyUnit));
    }

    public void setValue(int value) {
        valueSpinner.setSelection(currentValueSpinnerAdapter.getIndexByValue(value));
    }
}
