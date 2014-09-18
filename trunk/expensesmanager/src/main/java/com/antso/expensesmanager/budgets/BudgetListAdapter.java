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
        //TODO create a proper layout for budgets

        // Get the current ToDoItem
        final BudgetManager.BudgetInfo budgetInfo = budgetManager.getBudgetInfo().get(position);

        //Inflate the View for this ToDoItem
        // from todo_item.xml.
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.account_item, null, false);

        // Fill in specific ToDoItem data
        // Remember that the data that goes in this View
        // corresponds to the user interface elements defined
        // in the layout file

        // Display Title in TextView
        final LinearLayout colorView = (LinearLayout) itemLayout.findViewById(R.id.accountColor);
        colorView.setBackgroundColor(budgetInfo.budget.getColor());

        final TextView nameView = (TextView) itemLayout.findViewById(R.id.accountName);
        nameView.setText(budgetInfo.budget.getName());
        nameView.setTextColor(Color.BLACK);

        final TextView balanceView = (TextView) itemLayout.findViewById(R.id.accountBalance);
        String balance = Utils.getCurrencyString() + " " + budgetInfo.budget.getThreshold().setScale(2).toPlainString();
        balanceView.setText(balance);

        final TextView monthInView = (TextView) itemLayout.findViewById(R.id.accountMonthIn);
        String periodOut = "Expenses " + Utils.getCurrencyString() + " " +
                budgetInfo.periodOut.setScale(2).toPlainString();
        monthInView.setText(periodOut);
        monthInView.setTextColor(Color.BLUE);
//
//        final TextView monthOutView = (TextView) itemLayout.findViewById(R.id.accountMonthOut);
//        String monthOut = "Expenses " + Utils.getCurrencyString() + " " + budgetInfo.monthOut.setScale(2).toPlainString();
//        monthOutView.setText(monthOut);
//        monthOutView.setTextColor(Color.RED);
//
//        final TextView monthBalanceView = (TextView) itemLayout.findViewById(R.id.accountMonthBalance);
//        String monthBalance = "Total " + Utils.getCurrencyString() + " " + budgetInfo.monthBalance.setScale(2).toPlainString();
//        monthBalanceView.setText(monthBalance);
//        if (budgetInfo.monthBalance.compareTo(BigDecimal.ZERO) > 0) {
//            monthBalanceView.setTextColor(Color.GREEN);
//        } else if (budgetInfo.monthBalance.compareTo(BigDecimal.ZERO) < 0) {
//            monthBalanceView.setTextColor(Color.RED);
//        } else {
//            monthBalanceView.setTextColor(Color.GRAY);
//        }
        // Return the View you just created
        return itemLayout;

    }

}
