package com.antso.expenses.utils;

import com.antso.expenses.budgets.BudgetManager;

import java.util.Comparator;

public class BudgetInfoAlphabeticalComparator implements Comparator<BudgetManager.BudgetInfo> {

    @Override
    public int compare(BudgetManager.BudgetInfo lhs, BudgetManager.BudgetInfo rhs) {
        return lhs.budget.getName().compareTo(rhs.budget.getName());
    }
}
