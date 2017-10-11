package com.antso.expenses.statistics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.antso.expenses.R;
import com.antso.expenses.adapters.BudgetSpinnerAdapter;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.DateLabelFormatter;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Settings;
import com.antso.expenses.views_helpers.ButtonChangeSpinner;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetsStatisticsFragment extends Fragment {

    private Collection<Budget> budgets;
    private Map<String, BudgetDataPoint> budgetDataPointsByBudget;
    private BudgetSpinnerAdapter budgetSpinnerAdapter;
    private ButtonChangeSpinner budgetSpinner;
    private GraphView graph;
    private final static int maxViewportSize = 8;

    public BudgetsStatisticsFragment() {
        budgetDataPointsByBudget = new HashMap<>();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (budgetSpinner == null) {
            budgetSpinner = new ButtonChangeSpinner(getActivity());
        }

        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER().getBudgets();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            budgetSpinnerAdapter = BudgetSpinnerAdapter.create(getActivity(), R.layout.text_spinner_item,
                    budgets.toArray(new Budget[0]));
        }

        budgetSpinner.createView(R.id.statisticsBudgetSpinner, budgetSpinnerAdapter);
        budgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Budget budget = budgetSpinnerAdapter.getItem(position);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateChart(budget);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        budgetSpinner.setSelection(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics_budget_charts, container, false);
        graph = (GraphView) view.findViewById(R.id.statisticsBudgetGraph);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(false);
        graph.getGridLabelRenderer().setLabelFormatter(new DateLabelFormatter(Settings.getCurrencySymbol(getActivity()), true));
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setPadding(15); // pad all the statistics_account_charts
        return view;
    }

    private void updateChart(Budget budget) {
        BudgetDataPoint budgetData;
        if (!budgetDataPointsByBudget.containsKey(budget.getId())) {
            budgetData = getDataPoints(budget);
            budgetDataPointsByBudget.put(budget.getId(), budgetData);
        } else {
            budgetData = budgetDataPointsByBudget.get(budget.getId());
        }

        graph.removeAllSeries();

        if (budgetData.in.size() > 0) {
            BarGraphSeries<DataPoint> s1 = new BarGraphSeries<>(budgetData.in.toArray(new DataPoint[0]));
            BarGraphSeries<DataPoint> s2 = new BarGraphSeries<>(budgetData.out.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> s3 = new LineGraphSeries<>(budgetData.threshold.toArray(new DataPoint[0]));
            s1.setColor(MaterialColours.GREEN_500);
            s2.setColor(MaterialColours.RED_500);
            s3.setColor(MaterialColours.BLUE_500);
            s1.setSpacing(40);
            s2.setSpacing(40);

            int size = (budgetData.in.size() > maxViewportSize) ? maxViewportSize : budgetData.in.size();
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(size);

            int minXIndex = budgetData.out.size() - 1 - (size - 1);
            int maxXIndex = budgetData.out.size() - 1;
            graph.getViewport().setMinX(budgetData.out.get(minXIndex).getX());
            graph.getViewport().setMaxX(budgetData.out.get(maxXIndex).getX());

            graph.addSeries(s1);
            graph.addSeries(s2);
            graph.addSeries(s3);
        }

        graph.forceLayout();
    }

    private BudgetDataPoint getDataPoints(Budget budget) {
        if (budgetDataPointsByBudget.containsKey(budget.getId())) {
            return budgetDataPointsByBudget.get(budget.getId());
        }

        TransactionManager transactionManager = TransactionManager.TRANSACTION_MANAGER();
        transactionManager.resetGetBudgetNextPeriodTransactions(DateTime.now());

        final List<BigDecimal> ins = new ArrayList<>();
        final List<BigDecimal> outs = new ArrayList<>();
        final List<BigDecimal> ths = new ArrayList<>();
        final List<Date> dates = new ArrayList<>();

        SummaryTransaction st = transactionManager.getBudgetNextPeriodTransactionsSummary(budget.getId());
        while (st != null) {
            ins.add(st.getValueIn().negate());
            outs.add(st.getValueOut().subtract(st.getValueIn()));
            ths.add(budget.getThreshold());
            dates.add(st.getDate().toDate());
            st = transactionManager.getBudgetNextPeriodTransactionsSummary(budget.getId());
        }

        Collections.reverse(ins);
        Collections.reverse(outs);
        Collections.reverse(ths);
        Collections.reverse(dates);
        ArrayList<DataPoint> pointsIn = new ArrayList<>();
        ArrayList<DataPoint> pointsOut = new ArrayList<>();
        ArrayList<DataPoint> pointsBalance = new ArrayList<>();
        boolean onlyZeroSeen = true;

        for (int i = 0; i < ins.size(); i++) {
            if (onlyZeroSeen) {
                if (ins.get(i).compareTo(BigDecimal.ZERO) == 0 &&
                        outs.get(i).compareTo(BigDecimal.ZERO) == 0 &&
                        ths.get(i).compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                } else {
                    if (i > 0) {
                        pointsIn.add(new DataPoint(dates.get(i - 1), 0.0));
                        pointsOut.add(new DataPoint(dates.get(i - 1), 0.0));
                        pointsBalance.add(new DataPoint(dates.get(i - 1), 0.0));
                    }
                }
                onlyZeroSeen = false;
            }

            pointsIn.add(new DataPoint(dates.get(i), ins.get(i).doubleValue()));
            pointsOut.add(new DataPoint(dates.get(i), outs.get(i).doubleValue()));
            pointsBalance.add(new DataPoint(dates.get(i), ths.get(i).doubleValue()));
        }

        logSeries(budget.getId(), "IN", pointsIn);
        logSeries(budget.getId(), "OUT", pointsOut);
        logSeries(budget.getId(), "TH", pointsBalance);

        BudgetDataPoint dataPoint = new BudgetDataPoint(pointsIn, pointsOut, pointsBalance);
        budgetDataPointsByBudget.put(budget.getId(), dataPoint);
        return dataPoint;
    }

    private void logSeries(String id, String ctx, ArrayList<DataPoint> points) {
        StringBuilder builder = new StringBuilder("");
        for (DataPoint p : points) {
            DateTime date = new DateTime((long) p.getX());

            builder.append("{");
            builder.append(date.toString(DateTimeFormat.forPattern("dd-MM-yyy")));
            builder.append(" ");
            builder.append(p.getY());
            builder.append("} ");
        }
        Log.i("EXPENSES BUDGET CHARTS", ctx + " Id:" + id + " Data:" + builder.toString());
    }

    private static class BudgetDataPoint {
        public final ArrayList<DataPoint> in;
        public final ArrayList<DataPoint> out;
        public final ArrayList<DataPoint> threshold;

        public BudgetDataPoint(final ArrayList<DataPoint> in, final ArrayList<DataPoint> out, final ArrayList<DataPoint> threshold) {
            this.in = in;
            this.out = out;
            this.threshold = threshold;
        }
    }
}
