package com.antso.expensesmanager.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.antso.expensesmanager.entities.Budget;

import java.util.HashMap;
import java.util.Map;

public class BudgetSpinnerAdapter extends ArrayAdapter<Budget> {

    private Map<String, Integer> idToIndex;

    public BudgetSpinnerAdapter(Context context, int resource, Budget[] objects) {
        super(context, resource, objects);

        idToIndex = new HashMap<String, Integer>(objects.length);
        int i = 0;
        for (Budget budget : objects) {
            idToIndex.put(budget.getId(), i);
            i++;
        }
    }

    public int getIndexById(String id) {
        return idToIndex.get(id);
    }
}
