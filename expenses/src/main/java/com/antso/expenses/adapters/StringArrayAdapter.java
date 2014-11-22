package com.antso.expenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antso.expenses.R;

public class StringArrayAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater;

    protected StringArrayAdapter(Context context, int resource, String[] values) {
        super(context, resource, values);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static StringArrayAdapter create(Context context, int resource, String[] values) {
        return new StringArrayAdapter(context, resource, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.text_spinner_dropdown_item,
                R.id.textSpinnerDropdownItem);
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

        String item = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(item);

        return view;
    }

}
