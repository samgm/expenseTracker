package com.antso.expensesmanager.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;

import java.util.HashMap;
import java.util.Map;

public class BudgetSpinnerAdapter extends ArrayAdapter<Budget> {

    private Map<String, Integer> idToIndex;

    protected BudgetSpinnerAdapter(Context context, int resource, Budget[] budgets) {
        super(context, resource, budgets);

        idToIndex = new HashMap<String, Integer>(budgets.length);
        int i = 0;
        for (Budget budget : budgets) {
            idToIndex.put(budget.getId(), i);
            i++;
        }
    }

    public int getIndexById(String id) {
        return idToIndex.get(id);
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
}
