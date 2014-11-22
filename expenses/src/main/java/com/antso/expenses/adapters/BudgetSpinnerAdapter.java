package com.antso.expenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.views.CircleSectorView;

import java.util.HashMap;
import java.util.Map;

public class BudgetSpinnerAdapter extends ArrayAdapter<Budget> {

    private Map<String, Integer> idToIndex;
    private LayoutInflater mInflater;

    protected BudgetSpinnerAdapter(Context context, int resource, Budget[] budgets) {
        super(context, resource, budgets);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        idToIndex = new HashMap<String, Integer>(budgets.length);
        int i = 0;
        for (Budget budget : budgets) {
            idToIndex.put(budget.getId(), i);
            i++;
        }
    }

    public int getIndexById(String id) {
        if(id == null) {
            return 0;
        }

        Integer index = idToIndex.get(id);
        return (index != null) ? index : 0;
    }

    public static BudgetSpinnerAdapter create(Context context, int resource, Budget[] budgets) {
        return new BudgetSpinnerAdapter(context, resource, budgets);
    }

    public static BudgetSpinnerAdapter create(Context context, int resource, Budget[] budgets,
                                              Budget excludeBudget) {
        Budget[] finalBudgets = new Budget[budgets.length - 1];
        int i = 0;
        for (Budget budget : budgets) {
            if (!budget.getId().equals(excludeBudget.getId()) && i < finalBudgets.length) {
                finalBudgets[i] = budget;
                i++;
            }
        }

        return new BudgetSpinnerAdapter(context, resource, finalBudgets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.budget_spinner_item,
                R.id.budgetSpinnerItemText,
                R.id.budgetSpinnerItemColor);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent,
                R.layout.budget_spinner_dropdown_item,
                R.id.budgetSpinnerDropdownItemText,
                R.id.budgetSpinnerDropdownItemColor);
    }

    private View createView(int position, View convertView, ViewGroup parent,
                            int viewId, int textId, int colorId) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(viewId, parent, false);
        }

        Budget budget = getItem(position);
        TextView name = (TextView)view.findViewById(textId);
        name.setText(budget.getName());

        CircleSectorView color = (CircleSectorView)view.findViewById(colorId);
        color.setColor(budget.getColor());

        return view;
    }
}
