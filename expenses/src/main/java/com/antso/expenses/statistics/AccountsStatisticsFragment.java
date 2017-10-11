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
import org.joda.time.format.DateTimeFormat;

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
    private static final int maxViewportSize = 8;

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


        accountSpinner.createView(R.id.statisticsAccountSpinner, accountSpinnerAdapter);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics_account_charts, container, false);
        graph = (GraphView) view.findViewById(R.id.statisticsAccountGraph);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(false);
        graph.getGridLabelRenderer().setLabelFormatter(new DateLabelFormatter(Settings.getCurrencySymbol(getActivity()), false));
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setPadding(15); // pad all the statistics_account_charts
        return view;
    }

    private void updateChart(Account account) {
        AccountDataPoint accountData = getDataPoints(account);

        graph.removeAllSeries();

        if (accountData.in.size() > 0) {
            LineGraphSeries<DataPoint> s1 = new LineGraphSeries<>(accountData.in.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>(accountData.out.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> s3 = new LineGraphSeries<>(accountData.balance.toArray(new DataPoint[0]));
            s1.setColor(MaterialColours.GREEN_500);
            s2.setColor(MaterialColours.RED_500);
            s3.setColor(MaterialColours.BLUE_500);

            int size = (accountData.in.size() >= maxViewportSize) ? maxViewportSize : accountData.in.size();
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setNumHorizontalLabels(size);
            int minXIndex = accountData.in.size() - 1 - (size - 1);
            int maxXIndex = accountData.in.size() - 1;
            graph.getViewport().setMinX(accountData.in.get(minXIndex).getX());
            graph.getViewport().setMaxX(accountData.in.get(maxXIndex).getX());

            graph.addSeries(s1);
            graph.addSeries(s2);
            graph.addSeries(s3);
        }

        graph.forceLayout();

//        View graphAsView = graph;
//        graphAsView.getCh
//        for(int i=0; i<((ViewGroup)graphAsView).getChildCount(); ++i) {
//            View nextChild = ((ViewGroup)graphAsView).getChildAt(i);
//            Log.i("EXPENSES CHARTS", "Children Class " + nextChild.getClass() + " Children Width " + nextChild.getWidth());
//        }
    }

    private AccountDataPoint getDataPoints(Account account) {
        if (accountDataPointsByAccount.containsKey(account.getId())) {
            return accountDataPointsByAccount.get(account.getId());
        }

        TransactionManager transactionManager = TransactionManager.TRANSACTION_MANAGER();
        transactionManager.resetGetAccountNextPeriodTransactions(DateTime.now());

        final List<BigDecimal> ins = new ArrayList<>();
        final List<BigDecimal> outs = new ArrayList<>();
        final List<BigDecimal> balances = new ArrayList<>();
        final List<Date> dates = new ArrayList<>();

        SummaryTransaction st = transactionManager.getAccountNextPeriodTransactionsSummary(account.getId());
        while (st != null) {
            ins.add(st.getValueIn());
            outs.add(st.getValueOut());
            balances.add(st.getBalance());
            dates.add(st.getDate().toDate());
            st = transactionManager.getAccountNextPeriodTransactionsSummary(account.getId());
        }

        Collections.reverse(ins);
        Collections.reverse(outs);
        Collections.reverse(balances);
        Collections.reverse(dates);
        ArrayList<DataPoint> pointsIn = new ArrayList<>();
        ArrayList<DataPoint> pointsOut = new ArrayList<>();
        ArrayList<DataPoint> pointsBalance = new ArrayList<>();
        boolean onlyZeroSeen = true;

        for (int i = 0; i < ins.size(); i++) {
            if (onlyZeroSeen) {
                if (ins.get(i).compareTo(BigDecimal.ZERO) == 0 &&
                        outs.get(i).compareTo(BigDecimal.ZERO) == 0 &&
                        balances.get(i).compareTo(BigDecimal.ZERO) == 0) {
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
            pointsBalance.add(new DataPoint(dates.get(i), balances.get(i).doubleValue()));
        }

        logSeries(account.getId(), "IN", pointsIn);
        logSeries(account.getId(), "OUT", pointsOut);
        logSeries(account.getId(), "BAL", pointsBalance);

        AccountDataPoint dataPoint = new AccountDataPoint(pointsIn, pointsOut, pointsBalance);
        accountDataPointsByAccount.put(account.getId(), dataPoint);
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
        Log.i("EXPENSES ACCOUNT CHARTS", ctx + " Id:" + id +" Data:" + builder.toString());
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
