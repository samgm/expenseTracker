package com.antso.expenses.budgets;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.persistence.DatabaseHelper;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.transactions.TransactionUpdateEvent;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class BudgetManager extends Observable {
    private static volatile BudgetManager instance = null;

    private boolean started;
    private Map<String, BudgetInfo> budgets;
    private DatabaseHelper dbHelper = null;

    private BudgetManager() {
        budgets = new HashMap<String, BudgetInfo>();
    }

    public static synchronized BudgetManager BUDGET_MANAGER() {
        if (instance == null) {
            instance = new BudgetManager();
        }

        return instance;
    }

    public void start(Context context) {
        long start = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "BUDGET_MANAGER(" + this + ") Start begin " + start);

        if (started) {
            return;
        }

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);

            Collection<Budget> budgets = dbHelper.getBudgets();
            for (Budget budget : budgets) {
                addBudget(budget);
            }

            if (budgets.size() == 0) {
                createDefaultBudget();
            }

        }

        started = true;

        setChanged();
        Log.i("EXPENSES OBS", "BUDGET_MANAGER(" + this + ") observers: " + countObservers());
        notifyObservers(TransactionUpdateEvent.createStart());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "BUDGET_MANAGER(" + this + ") Start end " + end + "{" + (end - start) + "}");
    }

    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createStart());
    }

    private void createDefaultBudget() {
        Budget budget = new Budget("DEFAULT_BUDGET", "Budget", BigDecimal.ZERO, MaterialColours.GREY_500,
                1, TimeUnit.Month, Utils.now());
        dbHelper.insertBudget(budget);
        addBudget(budget);
    }

    public void stop() {
        budgets.clear();
        started = false;

        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void onTransactionAdded(Transaction transaction) {
        BudgetInfo budgetInfo = budgets.get(transaction.getBudgetId());
        if (budgetInfo != null) {
            budgetInfo.addTransaction(transaction);
        }
    }

    public void onTransactionDeleted(Transaction transaction) {
        BudgetInfo budgetInfo= budgets.get(transaction.getBudgetId());
        if (budgetInfo != null) {
            budgetInfo.removeTransaction(transaction);
        }
    }

    private void addBudget(Budget budget) {
        Collection<Transaction> transactions = TransactionManager.TRANSACTION_MANAGER()
                .getTransactionByBudget(budget.getId());
        BudgetInfo budgetInfo = new BudgetInfo(budget, transactions);
        budgets.put(budget.getId(), budgetInfo);
    }

    public void insertBudget(Budget budget) {
        dbHelper.insertBudget(budget);
        addBudget(budget);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public void updateBudget(Budget budget) {
        dbHelper.updateBudget(budget);
        budgets.remove(budget.getId());
        addBudget(budget);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public void removeBudget(Budget budget) {
        budgets.remove(budget.getId());
        dbHelper.deleteBudget(budget.getId());

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public int size() {
        return budgets.size();
    }

    public List<BudgetInfo> getBudgetInfo() {
        List<BudgetInfo> budgetInfoList = new ArrayList<BudgetInfo>(budgets.size());
        for(BudgetInfo info : budgets.values()) {
            budgetInfoList.add(info);
        }
        return budgetInfoList;
    }

    public BudgetInfo getBudgetInfo(String budgetId) {
        return budgets.get(budgetId);
    }

    public Map<String, Budget> getBudgetsByName() {
        Map<String, Budget> budgetByName = new HashMap<String, Budget>(budgets.size());
        for(BudgetInfo info : budgets.values()) {
            if(!budgetByName.containsKey(info.budget.getName())) {
                budgetByName.put(info.budget.getName(), info.budget);
            } else {
                Log. i("BudgetManager", "Error creating budgetByName map: Budget with this name already added {Name " + info.budget.getName() + "}");
            }
        }
        return budgetByName;
    }

    public Collection<Budget> getBudgets() {
        return dbHelper.getBudgets();
    }

    public class BudgetInfo {
        public Budget budget;

        public BigDecimal periodIn;
        public BigDecimal periodOut;
        public BigDecimal periodBalance;

        public BudgetInfo(Budget budget, Collection<Transaction> transactions) {
            this.budget = budget;
            refresh(Utils.now(), transactions, true, false);
        }

        public void addTransaction(Transaction transaction) {
            Collection<Transaction> exploded = new ArrayList<Transaction>();
            if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
                exploded = TransactionManager.explodeRecurrentTransaction(transaction, Utils.now());
            }
            exploded.add(transaction);
            refresh(Utils.now(), exploded, false, false);
        }

        public void removeTransaction(Transaction transaction) {
            Collection<Transaction> exploded = new ArrayList<Transaction>();
            if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
                exploded = TransactionManager.explodeRecurrentTransaction(transaction, Utils.now());
            }
            exploded.add(transaction);
            refresh(Utils.now(), exploded, false, true);
        }

        public void refresh(DateTime currentDateTime, Collection<Transaction> transactions, boolean reset, boolean remove) {
            if (reset) {
                periodIn = BigDecimal.ZERO;
                periodOut = BigDecimal.ZERO;
                periodBalance = BigDecimal.ZERO;
            }

            Pair<DateTime, DateTime> periodStartEnd = getPeriodStartEnd(currentDateTime);
            if (periodStartEnd == null) {
                return;
            }
            DateTime periodStart = periodStartEnd.first;
            DateTime periodEnd = periodStartEnd.second;

            for (Transaction transaction : transactions) {
                if(Utils.isAfterOrEqual(transaction.getDate(), periodStart) &&
                        Utils.isBefore(transaction.getDate(), periodEnd)){

                    BigDecimal value = transaction.getValue();
                    if (remove) {
                        value = value.negate();
                    }

                    if (transaction.getDirection().equals(TransactionDirection.Out)) {
                        periodBalance = periodBalance.subtract(value);
                        periodOut = periodOut.add(value);
                    }
                    if (transaction.getDirection().equals(TransactionDirection.In)) {
                        periodBalance = periodBalance.add(value);
                        periodIn = periodIn.add(value);
                    }
                }
            }
        }

        public Pair<DateTime, DateTime> getPeriodStartEnd(DateTime currentDateTime) {
            int iterationNum = 1;
            DateTime start = budget.getPeriodStart();
            int periodLength = budget.getPeriodLength();
            TimeUnit periodUnit = budget.getPeriodUnit();
            if (periodUnit.equals(TimeUnit.Undef)) {
                return  null;
            }
            DateTime periodStart = start;
            DateTime periodStartOld = start;
            while (Utils.isBeforeOrEqual(periodStart, currentDateTime)) {
                periodStartOld = periodStart;
                switch (periodUnit) {
                    case Day:
                        periodStart = start.plusDays(periodLength * iterationNum);
                        break;
                    case Week:
                        periodStart = start.plusWeeks(periodLength * iterationNum);
                        break;
                    case Month:
                        periodStart = start.plusMonths(periodLength * iterationNum);
                        break;
                    case Year:
                        periodStart = start.plusYears(periodLength * iterationNum);
                        break;
                }
                iterationNum++;
            }

            return  new Pair<DateTime, DateTime>(periodStartOld.withTimeAtStartOfDay(),
                    periodStart.withTimeAtStartOfDay());
        }

        public int getPercentage() {
            BigDecimal balance = periodOut.subtract(periodIn);
            if(balance.compareTo(BigDecimal.ZERO) < 0) {
                return 0;
            } else {
                double result = balance.doubleValue();
                result = result / (budget.getThreshold().doubleValue()) * 100;
                return (int) result;
            }
        }

    }

}
