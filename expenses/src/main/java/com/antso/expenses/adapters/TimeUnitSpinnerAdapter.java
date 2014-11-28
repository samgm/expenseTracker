package com.antso.expenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.enums.TimeUnit;

import java.util.HashMap;
import java.util.Map;

public class TimeUnitSpinnerAdapter extends ArrayAdapter<TimeUnit> {

    private Map<TimeUnit, Integer> valueToIndex;
    private final LayoutInflater mInflater;
    private final Context context;

    protected TimeUnitSpinnerAdapter(Context context, int resource, TimeUnit timeUnits[]) {
        super(context, resource, timeUnits);

        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.text_spinner_item,
                R.id.textSpinnerItem);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.text_spinner_dropdown_item,
                R.id.textSpinnerDropdownItem);
    }

    private View createView(int position, View convertView, ViewGroup parent,
                            int viewId, int textId) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(viewId, parent, false);
        }

        TimeUnit timeUnit = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(timeUnit.getLangStringValue(context, false));

        return view;
    }

}
