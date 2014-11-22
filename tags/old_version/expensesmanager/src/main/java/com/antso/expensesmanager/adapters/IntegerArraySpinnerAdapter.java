package com.antso.expensesmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antso.expensesmanager.R;

import java.util.HashMap;
import java.util.Map;

public class IntegerArraySpinnerAdapter extends ArrayAdapter<Integer> {

    private Map<Integer, Integer> valueToIndex;
    private LayoutInflater mInflater;

    protected IntegerArraySpinnerAdapter(Context context, int resource, Integer[] values) {
        super(context, resource, values);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        Integer integer = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(integer.toString());

        return view;
    }

}
