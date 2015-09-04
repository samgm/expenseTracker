package com.antso.expenses.budgets;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.CircleSectorView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class BudgetListAdapter extends BaseAdapter  implements Observer {
    private final BudgetManager budgetManager;
    private final Context context;

    private volatile CircleSectorView selectedItemView = null;
    private volatile int selectedItemIndex = -1;
    private OnSelectionChanged onSelectionChangedHandler;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final BudgetManager.BudgetInfo budgetInfo = budgetManager.getBudgetInfo().get(position);

        List<Integer> percentages = getPercentages(budgetInfo);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout budgetLayout = (LinearLayout) inflater.inflate(R.layout.budget_item, null, false);

        final CircleSectorView color = (CircleSectorView) budgetLayout.findViewById(R.id.budgetColor);
        final CircleSectorView colorOld = (CircleSectorView) budgetLayout.findViewById(R.id.budgetColorOld);
        final CircleSectorView colorOlder = (CircleSectorView) budgetLayout.findViewById(R.id.budgetColorOlder);
        color.setColor(budgetInfo.budget.getColor());
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItemIndex != -1) {
                    selectedItemView.setSelected(false);
                }

                if (position != selectedItemIndex) {
                    color.setSelected(true);
                    selectedItemView = (CircleSectorView) budgetLayout.findViewById(R.id.budgetColor);
                    selectedItemIndex = position;
                } else {
                    selectedItemView = null;
                    selectedItemIndex = -1;
                }

                if (onSelectionChangedHandler != null) {
                    onSelectionChangedHandler.onSelectionChanged(selectedItemIndex);
                }
            }
        });
        colorOld.setColor(budgetInfo.budget.getColor());
        colorOlder.setColor(budgetInfo.budget.getColor());
        color.setCirclePercentage(percentages.get(0));
        colorOld.setCirclePercentage(percentages.get(1));
        colorOlder.setCirclePercentage(percentages.get(2));

        final TextView name = (TextView) budgetLayout.findViewById(R.id.budgetName);
        name.setText(budgetInfo.budget.getName());

        final TextView balance = (TextView) budgetLayout.findViewById(R.id.budgetBalance);
        String balanceStr = Utils.getCurrencyString(context) + " " +
                budgetInfo.periodBalance.setScale(2).toPlainString();
        balance.setText(balanceStr);

        final TextView period = (TextView) budgetLayout.findViewById(R.id.budgetPeriod);
        String length = (budgetInfo.budget.getPeriodLength() > 1) ? (budgetInfo.budget.getPeriodLength() + " ") : "";
        String periodStr = context.getString(R.string.every_label) + " " + length +
                budgetInfo.budget.getPeriodUnit().getLangStringValue(context, (budgetInfo.budget.getPeriodLength() > 1));
        period.setText(periodStr);

        final TextView threshold = (TextView) budgetLayout.findViewById(R.id.budgetThreshold);
        String thresholdStr = Utils.getCurrencyString(context) + " " +
                budgetInfo.budget.getThreshold().setScale(2).toPlainString();
        threshold.setText(thresholdStr);

        return budgetLayout;
    }

    private List<Integer> getPercentages(BudgetManager.BudgetInfo budgetInfo) {
        DateTime periodStart = budgetInfo.getPeriodStartEnd(DateTime.now()).first;
        TransactionManager tm = TransactionManager.TRANSACTION_MANAGER();
        tm.resetGetBudgetNextPeriodTransactions(periodStart.minusDays(1));

        ArrayList<Integer> percentages = new ArrayList<>();
        percentages.add(budgetInfo.getPercentage());

        SummaryTransaction st = tm.getBudgetNextPeriodTransactionsSummary(budgetInfo.budget.getId());
        if (st != null) {
            percentages.add(Utils.getPercentage(st.getValueIn(), st.getValueOut(), budgetInfo.budget.getThreshold()));
        } else {
            percentages.add(0);
        }

        st = tm.getBudgetNextPeriodTransactionsSummary(budgetInfo.budget.getId());
        if (st != null) {
            percentages.add(Utils.getPercentage(st.getValueIn(), st.getValueOut(), budgetInfo.budget.getThreshold()));
        } else {
            percentages.add(0);
        }

        return percentages;
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

    public Budget getSelectedBudget() {
        return ((BudgetManager.BudgetInfo)getItem(selectedItemIndex)).budget;
    }

    public void resetSelection() {
        if (selectedItemIndex != -1) {
            selectedItemView.setSelected(false);
            selectedItemView = null;
            selectedItemIndex = -1;

            if (onSelectionChangedHandler != null) {
                onSelectionChangedHandler.onSelectionChanged(selectedItemIndex);
            }
        }
    }

    void setOnSelectionChangeHandler(OnSelectionChanged handler) {
        this.onSelectionChangedHandler = handler;
    }

    public interface OnSelectionChanged {
        void onSelectionChanged(int selectedItemIndex);
    }
}
