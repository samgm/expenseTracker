package com.antso.expensesmanager.budgets;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;


public class BudgetListAdapter extends BaseAdapter {
    private final BudgetManager budgetManager;
    private final Context context;

    public BudgetListAdapter(Context context, BudgetManager budgetManager) {
        this.context = context;
        this.budgetManager = budgetManager;
    }

    @Override
    public int getCount() {
        return budgetManager.getBudgetInfo().size();
    }

    @Override
    public Object getItem(int pos) {
        return budgetManager.getBudgetInfo().get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BudgetManager.BudgetInfo budgetInfo = budgetManager.getBudgetInfo().get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout budgetLayout = (LinearLayout) inflater.inflate(R.layout.budget_item, null, false);

        final CircleSectorView color = (CircleSectorView) budgetLayout.findViewById(R.id.budgetColor);
        color.setColor(budgetInfo.budget.getColor());

        final TextView name = (TextView) budgetLayout.findViewById(R.id.budgetName);
        name.setText(budgetInfo.budget.getName());

        final TextView balance = (TextView) budgetLayout.findViewById(R.id.budgetBalance);
        String balanceStr = Utils.getCurrencyString() + " " +
                budgetInfo.periodBalance.setScale(2).toPlainString();
        balance.setText(balanceStr);

        final TextView period = (TextView) budgetLayout.findViewById(R.id.budgetPeriod);
        String periodStr = budgetInfo.budget.getPeriodLength() +
                budgetInfo.budget.getPeriodUnit().getStringValue();
        period.setText(periodStr);

        final TextView threshold = (TextView) budgetLayout.findViewById(R.id.budgetThreshold);
        String thresholdStr = Utils.getCurrencyString() + " " +
                budgetInfo.budget.getThreshold().setScale(2).toPlainString();
        threshold.setText(thresholdStr);

        return budgetLayout;
    }

}
