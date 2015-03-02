package com.antso.expenses.statistics;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.adapters.AccountSpinnerAdapter;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.DateLabelFormatter;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Settings;
import com.antso.expenses.views_helpers.ButtonChangeSpinner;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsStatisticsFragment extends Fragment {

    private Collection<Account> accounts;
    private Map<String, AccountDataPoint> accountDataPointsByAccount;
    private AccountSpinnerAdapter accountSpinnerAdapter;
    private ButtonChangeSpinner accountSpinner;
    private GraphView graph;
    private final int maxViewportSize = 8;

    public AccountsStatisticsFragment() {
        accountDataPointsByAccount = new HashMap<>();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (accountSpinner == null) {
            accountSpinner = new ButtonChangeSpinner(getActivity());
        }

        if (accounts == null) {
            accounts = AccountManager.ACCOUNT_MANAGER().getAccounts();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            accountSpinnerAdapter = AccountSpinnerAdapter.create(getActivity(), R.layout.text_spinner_item,
                    accounts.toArray(new Account[0]));
        }

        accountSpinner.createView(R.id.statisticsAccountSpinner, R.id.statisticsAccountButton, accountSpinnerAdapter);
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Account account = accountSpinnerAdapter.getItem(position);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateChart(account);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        accountSpinner.setSelection(0);
    }

    private void updateChart(Account account) {
        AccountDataPoint accountData = null;
        if (!accountDataPointsByAccount.containsKey(account.getId())) {
            accountData = getDataPoints(account.getId());
            accountDataPointsByAccount.put(account.getId(), accountData);
        } else {
            accountData = accountDataPointsByAccount.get(account.getId());
        }

        graph.removeAllSeries();

        if (accountData.in.size() > 0) {
            LineGraphSeries<DataPoint> s1 = new LineGraphSeries<>(accountData.in.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>(accountData.out.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> s3 = new LineGraphSeries<>(accountData.balance.toArray(new DataPoint[0]));
            s1.setColor(MaterialColours.GREEN_500);
            s2.setColor(MaterialColours.RED_500);
            s3.setColor(MaterialColours.BLUE_500);

            int size = (accountData.in.size() > maxViewportSize) ? maxViewportSize : accountData.in.size();
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(size);
            graph.getViewport().setMinX(accountData.in.get(accountData.in.size() - 1 - size).getX());
            graph.getViewport().setMaxX(accountData.in.get(accountData.in.size() - 1).getX());
            graph.getViewport().setMinY(minOfThree(s1.getLowestValueY(), s2.getLowestValueY(), s3.getLowestValueY()));
            graph.getViewport().setMaxY(maxOfThree(s1.getHighestValueY(), s2.getHighestValueY(), s3.getHighestValueY()));

            graph.addSeries(s1);
            graph.addSeries(s2);
            graph.addSeries(s3);
        }

        graph.forceLayout();
    }

    private double maxOfThree(double v1, double v2, double v3) {
        double max = Math.max(v1, v2);
        max = Math.max(max, v3);
        int maxInt = (int) Math.ceil(max);

        return (double) maxInt;
    }

    private double minOfThree(double v1, double v2, double v3) {
        double min = Math.min(v1, v2);
        min = Math.min(min, v3);
        int minInt = (int) Math.ceil(min);

        return (double) (minInt < 0 ? minInt : 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics_acctount_charts, container, false);
        graph = (GraphView) view.findViewById(R.id.graph);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(false);
        graph.getGridLabelRenderer().setLabelFormatter(new DateLabelFormatter(Settings.getCurrencySymbol(getActivity())));
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setPadding(15); // pad all the statistics_account_charts

        return view;
    }

    private AccountDataPoint getDataPoints(String accountId) {
        TransactionManager transactionManager = TransactionManager.TRANSACTION_MANAGER();
        transactionManager.resetGetAccountNextPeriodTransactions(DateTime.now());

        final List<BigDecimal> ins = new ArrayList<>();
        final List<BigDecimal> outs = new ArrayList<>();
        final List<BigDecimal> balances = new ArrayList<>();
        final List<Date> dates = new ArrayList<>();

        SummaryTransaction st = transactionManager.getAccountNextPeriodTransactionsSummary(accountId);
        while (st != null) {
            ins.add(st.getValueIn());
            outs.add(st.getValueOut());
            balances.add(st.getBalance());
            dates.add(st.getDate().toDate());
            st = transactionManager.getAccountNextPeriodTransactionsSummary(accountId);
        }

        Collections.reverse(ins);
        Collections.reverse(outs);
        Collections.reverse(balances);
        Collections.reverse(dates);
        ArrayList<DataPoint> pointsIn = new ArrayList<>();
        ArrayList<DataPoint> pointsOut = new ArrayList<>();
        ArrayList<DataPoint> pointsBalance = new ArrayList<>();
//        boolean onlyZeroSeen = true;

        for (int i = 0; i < ins.size(); i++) {
//            if ( onlyZeroSeen &&
//                    ins.get(i).compareTo(BigDecimal.ZERO) == 0 &&
//                    outs.get(i).compareTo(BigDecimal.ZERO) == 0 &&
//                    balances.get(i).compareTo(BigDecimal.ZERO) == 0) {
//
//                pointsIn.add(new DataPoint(dates.get(i), 1000 + i));
//                pointsOut.add(new DataPoint(dates.get(i), 1000 - i));
//                pointsBalance.add(new DataPoint(dates.get(i), 1000));
//
//                continue;
//            }

//            if (onlyZeroSeen && i > 0) {
//                pointsIn.add(new DataPoint(dates.get(i - 1), 0.0));
//                pointsOut.add(new DataPoint(dates.get(i - 1), 0.0));
//                pointsBalance.add(new DataPoint(dates.get(i - 1), 0.0));
//                onlyZeroSeen = false;
//            }

            pointsIn.add(new DataPoint(dates.get(i), ins.get(i).doubleValue()));
            pointsOut.add(new DataPoint(dates.get(i), outs.get(i).doubleValue()));
            pointsBalance.add(new DataPoint(dates.get(i), balances.get(i).doubleValue()));
        }

        logSeries(accountId, pointsIn);
        logSeries(accountId, pointsOut);
        logSeries(accountId, pointsBalance);

        return new AccountDataPoint(pointsIn, pointsOut, pointsBalance);
    }

    private void logSeries(String id, ArrayList<DataPoint> points) {
        Log.i("EXPENSES CHARTS", "Series Id " + id);
        StringBuilder builder = new StringBuilder("");
        for (DataPoint p : points) {
            builder.append("{");
            builder.append(p.getX());
            builder.append(" ");
            builder.append(p.getY());
            builder.append("} ");
        }
        Log.i("EXPENSES CHARTS", "Series Data " + builder.toString());
    }

    private static class AccountDataPoint {
        public final ArrayList<DataPoint> in;
        public final ArrayList<DataPoint> out;
        public final ArrayList<DataPoint> balance;

        public AccountDataPoint(final ArrayList<DataPoint> in, final ArrayList<DataPoint> out, final ArrayList<DataPoint> balance) {
            this.in = in;
            this.out = out;
            this.balance = balance;
        }
    }
}
