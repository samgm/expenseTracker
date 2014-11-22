package com.antso.expenses.budgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.CircleSectorView;

import java.util.Observable;
import java.util.Observer;


public class BudgetListAdapter extends BaseAdapter  implements Observer {
    private final BudgetManager budgetManager;
    private final Context context;

    public BudgetListAdapter(Context context, BudgetManager budgetManager) {
        this.context = context;
        this.budgetManager = budgetManager;

        BudgetManager.BUDGET_MANAGER().addObserver(this);
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
        color.setCirclePercentage(budgetInfo.getPercentage());

        final TextView name = (TextView) budgetLayout.findViewById(R.id.budgetName);
        name.setText(budgetInfo.budget.getName());

        final TextView balance = (TextView) budgetLayout.findViewById(R.id.budgetBalance);
        String balanceStr = Utils.getCurrencyString(context) + " " +
                budgetInfo.periodBalance.setScale(2).toPlainString();
        balance.setText(balanceStr);

        final TextView period = (TextView) budgetLayout.findViewById(R.id.budgetPeriod);
        String periodStr = budgetInfo.budget.getPeriodLength() +
                budgetInfo.budget.getPeriodUnit().getLangStringValue(context);
        period.setText(periodStr);

        final TextView threshold = (TextView) budgetLayout.findViewById(R.id.budgetThreshold);
        String thresholdStr = Utils.getCurrencyString(context) + " " +
                budgetInfo.budget.getThreshold().setScale(2).toPlainString();
        threshold.setText(thresholdStr);

        return budgetLayout;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof BudgetManager) {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    public void onDestroy() {
        Log.i("EXPENSES OBS", this.getClass() + " deleted observer (" +
                BudgetManager.BUDGET_MANAGER() + ")");
        BudgetManager.BUDGET_MANAGER().deleteObserver(this);
    }

}
