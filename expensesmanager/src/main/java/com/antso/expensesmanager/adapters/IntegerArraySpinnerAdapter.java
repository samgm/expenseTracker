package com.antso.expensesmanager.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.Map;

public class IntegerArraySpinnerAdapter extends ArrayAdapter<Integer> {

    private Map<Integer, Integer> valueToIndex;

    protected IntegerArraySpinnerAdapter(Context context, int resource, Integer[] values) {
        super(context, resource, values);

        valueToIndex = new HashMap<Integer, Integer>(values.length);
        int i = 0;
        for (Integer val : values) {
            valueToIndex.put(val, i);
            i++;
        }
    }

    public int getIndexByValue(Integer id) {
        if (id == null) {
            return 0;
        }

        Integer index = valueToIndex.get(id);
        return (index != null) ? index : 0;
    }

    public static IntegerArraySpinnerAdapter create(Context context, int resource, Integer[] values) {
        return new IntegerArraySpinnerAdapter(context, resource, values);
    }
}
