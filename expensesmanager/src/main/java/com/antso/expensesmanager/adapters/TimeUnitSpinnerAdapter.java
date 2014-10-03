package com.antso.expensesmanager.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.antso.expensesmanager.enums.TimeUnit;

import java.util.HashMap;
import java.util.Map;

public class TimeUnitSpinnerAdapter extends ArrayAdapter<TimeUnit> {

    private Map<TimeUnit, Integer> valueToIndex;

    protected TimeUnitSpinnerAdapter(Context context, int resource, TimeUnit timeUnits[]) {
        super(context, resource, timeUnits);

        valueToIndex = new HashMap<TimeUnit, Integer>(timeUnits.length);
        int i = 0;
        for (TimeUnit unit : timeUnits) {
            valueToIndex.put(unit, i);
            i++;
        }
    }

    public int getIndexByValue(TimeUnit value) {
        if (value == null) {
            return 0;
        }

        Integer index = valueToIndex.get(value);
        return (index != null) ? index : 0;
    }

    public static TimeUnitSpinnerAdapter create(Context context, int resource) {
        return new TimeUnitSpinnerAdapter(context, resource, TimeUnit.valuesButUndef());
    }
}
