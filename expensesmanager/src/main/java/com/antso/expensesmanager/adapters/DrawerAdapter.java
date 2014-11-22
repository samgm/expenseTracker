package com.antso.expensesmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.antso.expensesmanager.NavigationDrawerFragment;
import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.views.CircleSectorView;

import java.util.HashMap;
import java.util.Map;

public class DrawerAdapter extends ArrayAdapter<NavigationDrawerFragment.DrawerItem> {

    private LayoutInflater mInflater;


    protected DrawerAdapter(Context context, int resource, NavigationDrawerFragment.DrawerItem[] items) {
        super(context, resource, items);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static DrawerAdapter create(Context context, int resource, NavigationDrawerFragment.DrawerItem[] items) {
        return new DrawerAdapter(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.drawer_item,
                R.id.drawerItemText,
                R.id.drawerItemIcon);
    }

    private View createView(int position, View convertView, ViewGroup parent,
                            int viewId, int textId, int iconId) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(viewId, parent, false);
        }

        NavigationDrawerFragment.DrawerItem item = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(item.text);

        ImageView separator = (ImageView)view.findViewById(R.id.drawerItemSeparator);
        separator.setVisibility(item.separator ? View.VISIBLE : View.GONE);

        if (item.icon != -1) {
            ImageView icon = (ImageView) view.findViewById(iconId);
            icon.setImageResource(item.icon);
        }
        return view;
    }
}
