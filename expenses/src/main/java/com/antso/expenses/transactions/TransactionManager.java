package com.antso.expenses.transactions;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.SummaryTransaction;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.persistence.DatabaseHelper;
import com.antso.expenses.utils.TransactionByDateComparator;
import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class TransactionManager extends Observable {
    private static volatile TransactionManager instance = null;

    private boolean started = false;

    private DatabaseHelper dbHelper = null;
    private Set<String> descriptionsArray = new HashSet<String>();

    private List<Transaction> inTransaction = new ArrayList<Transaction>();
    private List<Transaction> outTransaction = new ArrayList<Transaction>();
    private List<Transaction> transferTransaction = new ArrayList<Transaction>();
    private Map<String, Transaction> transactionById = new HashMap<String, Transaction>();
    private Map<String, List<Transaction>> transactionByAccount = new HashMap<String, List<Transaction>>();
    private Map<String, List<Transaction>> transactionByBudget = new HashMap<String, List<Transaction>>();

    private String revenueSummaryDescription = "";
    private String expenseSummaryDescription = "";
    private String totalSummaryDescription = "";

    private DateTime firstTransactionDate = Utils.now();

    private TransactionManager() {
    }

    public static synchronized TransactionManager TRANSACTION_MANAGER() {
        if (instance == null) {
            instance = new TransactionManager();
            Log.i("EXPENSES OBS", "TRANSACTION_MANAGER(" + instance + ") instantiated");
        }

        return instance;
    }

    public void start(Context context) {
        long start = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER(" + this + ") Start begin " + start);

        if (started) {
            return;
        }
        revenueSummaryDescription = context.getText(R.string.revenue_label).toString();
        expenseSummaryDescription = context.getText(R.string.expense_label).toString();
        totalSummaryDescription = context.getText(R.string.total_label).toString();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }

        Collection<Transaction> result = dbHelper.getTransactions();
        for (Transaction t : result) {
            addTransaction(t);
            descriptionsArray.add(t.getDescription());
            if(firstTransactionDate.isAfter(t.getDate())) {
                firstTransactionDate = t.getDate();
            }
        }

        started = true;

        setChanged();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER(" + this + ") observers: " + countObservers());
        notifyObservers(TransactionUpdateEvent.createStart());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER(" + this + ") Start end " + end + "{" + (end - start) + "}");
    }

    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createStart());
    }

    public void stop() {
        super.deleteObservers();

        descriptionsArray.clear();
        inTransaction.clear();
        outTransaction.clear();
        transferTransaction.clear();
        transactionById.clear();
        transactionByAccount.clear();
        transactionByBudget.clear();

        started = false;

        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void insertTransaction(Transaction... transactions) {
        for(Transaction transaction : transactions) {
            dbHelper.insertTransactions(transaction);

            addTransaction(transaction);
            descriptionsArray.add(transaction.getDescription());

            AccountManager.ACCOUNT_MANAGER().onTransactionAdded(transaction);
            BudgetManager.BUDGET_MANAGER().onTransactionAdded(transaction);
        }

        setChanged();
        notifyObservers(TransactionUpdateEvent.createAdd(transactions));
    }

    public void removeTransaction(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            if (transaction.getLinkedTransactionId() != null &&
                    !transaction.getLinkedTransactionId().isEmpty()) {
                Transaction linked = getTransactionById(transaction.getLinkedTransactionId());
                delTransaction(linked);
                dbHelper.deleteTransaction(transaction.getLinkedTransactionId());

                AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(linked);
                BudgetManager.BUDGET_MANAGER().onTransactionDeleted(linked);
            }
            if (transaction.getFeeTransactionId() != null &&
                    !transaction.getFeeTransactionId().isEmpty()) {
                Transaction fee = getTransactionById(transaction.getFeeTransactionId());
                delTransaction(fee);
                dbHelper.deleteTransaction(transaction.getFeeTransactionId());

                AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(fee);
                BudgetManager.BUDGET_MANAGER().onTransactionDeleted(fee);
            }
            delTransaction(transaction);
            dbHelper.deleteTransaction(transaction.getId());

            AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(transaction);
            BudgetManager.BUDGET_MANAGER().onTransactionDeleted(transaction);
        }

        setChanged();
        notifyObservers(TransactionUpdateEvent.createDel(transactions));
    }

    public void updateTransaction(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            Transaction oldTransaction = transactionById.get(transaction.getId());
            dbHelper.updateTransaction(transaction);

            delTransaction(oldTransaction);
            addTransaction(transaction);
            descriptionsArray.add(transaction.getDescription());

            AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(oldTransaction);
            BudgetManager.BUDGET_MANAGER().onTransactionDeleted(oldTransaction);
            AccountManager.ACCOUNT_MANAGER().onTransactionAdded(transaction);
            BudgetManager.BUDGET_MANAGER().onTransactionAdded(transaction);
        }

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(transactions));
    }

    private void addTransaction(Transaction t) {
        Collection<Transaction> exploded = new ArrayList<Transaction>();
        if (t.getRecurrent() && !t.isAutoGenerated()) {
            exploded.addAll(explodeRecurrentTransaction(t, Utils.now()));
        }

        transactionById.put(t.getId(), t);

        if (!transactionByAccount.containsKey(t.getAccountId())) {
            transactionByAccount.put(t.getAccountId(), new ArrayList<Transaction>());
        }
        List<Transaction> accountTransactions = transactionByAccount.get(t.getAccountId());
        accountTransactions.add(t);
        if (!exploded.isEmpty()) {
            accountTransactions.addAll(exploded);
        }
        Collections.sort(accountTransactions, new TransactionByDateComparator());

        if (!transactionByBudget.containsKey(t.getBudgetId())) {
            transactionByBudget.put(t.getBudgetId(), new ArrayList<Transaction>());
        }
        List<Transaction> budgetTransactions = transactionByBudget.get(t.getBudgetId());
        budgetTransactions.add(t);
        if (!exploded.isEmpty()) {
            budgetTransactions.addAll(exploded);
        }
        Collections.sort(budgetTransactions, new TransactionByDateComparator());

        if (t.getType().equals(TransactionType.Transfer)) {
            transferTransaction.add(t);
        } else {
            if (t.getDirection().equals(TransactionDirection.In)) {
                inTransaction.add(t);
            }
            if (t.getDirection().equals(TransactionDirection.Out)) {
                outTransaction.add(t);
            }
        }
    }

    private void delTransaction(Transaction t) {
        Collection<Transaction> exploded = new ArrayList<Transaction>();
        if (t.getRecurrent() && !t.isAutoGenerated()) {
            exploded.addAll(explodeRecurrentTransaction(t, Utils.now()));
        }

        transactionById.remove(t.getId());

        if (transactionByAccount.containsKey(t.getAccountId())) {
            transactionByAccount.get(t.getAccountId()).remove(t);
            if (t.getRecurrent()) {
                List<Transaction> accountTransactions = transactionByAccount.get(t.getAccountId());
                accountTransactions.removeAll(exploded);
            }
        }

        if (transactionByBudget.containsKey(t.getBudgetId())) {
            transactionByBudget.get(t.getBudgetId()).remove(t);
            if (t.getRecurrent()) {
                List<Transaction> budgetTransactions = transactionByBudget.get(t.getBudgetId());
                budgetTransactions.removeAll(exploded);
            }
        }

        if (t.getType().equals(TransactionType.Transfer)) {
            transferTransaction.remove(t);
        } else {
            if (t.getDirection().equals(TransactionDirection.In)) {
                inTransaction.remove(t);
            }
            if (t.getDirection().equals(TransactionDirection.Out)) {
                outTransaction.remove(t);
            }
        }
    }

    public void replaceAccount(String fromAccountId, String toAccountId) {
        if (transactionByAccount.containsKey(fromAccountId)) {
            List<Transaction> transactionsCopy =
                    new ArrayList<Transaction>(transactionByAccount.get(fromAccountId));
            transactionByAccount.remove(fromAccountId);
            for (Transaction transaction : transactionsCopy) {
                if (!transaction.isAutoGenerated()) {
                    AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(transaction);
                    transaction.setAccount(toAccountId);
                    dbHelper.updateTransaction(transaction);
                    AccountManager.ACCOUNT_MANAGER().onTransactionAdded(transaction);
                }
            }
            if (!transactionByAccount.containsKey(toAccountId)) {
                transactionByAccount.put(toAccountId, new ArrayList<Transaction>());
            }
            transactionByAccount.get(toAccountId).addAll(transactionsCopy);
        }
    }

    public void replaceBudget(String fromBudgetId, String toBudgetId) {
        if (transactionByBudget.containsKey(fromBudgetId)) {
            List<Transaction> transactionsCopy =
                    new ArrayList<Transaction>(transactionByBudget.get(fromBudgetId));
            transactionByBudget.remove(fromBudgetId);
            for (Transaction transaction : transactionsCopy) {
                if (!transaction.isAutoGenerated()) {
                    BudgetManager.BUDGET_MANAGER().onTransactionDeleted(transaction);
                    transaction.setBudget(toBudgetId);
                    dbHelper.updateTransaction(transaction);
                    BudgetManager.BUDGET_MANAGER().onTransactionAdded(transaction);
                }
            }
            if (!transactionByBudget.containsKey(toBudgetId)) {
                transactionByBudget.put(toBudgetId, new ArrayList<Transaction>());
            }
            transactionByBudget.get(toBudgetId).addAll(transactionsCopy);
        }
    }

    public void removeTransactionByAccount(String accountId) {
        if (transactionByAccount.containsKey(accountId)) {
            List<Transaction> transactionsCopy =
                    new ArrayList<Transaction>(transactionByAccount.get(accountId));
            for (Transaction transaction : transactionsCopy) {
                delTransaction(transaction);
                dbHelper.deleteTransaction(transaction.getId());
                AccountManager.ACCOUNT_MANAGER().onTransactionDeleted(transaction);
                BudgetManager.BUDGET_MANAGER().onTransactionDeleted(transaction);
            }
            transactionByAccount.remove(accountId);
        }
    }

    public Transaction getTransactionById(String id) {
        return transactionById.get(id);
    }

    public List<Transaction> getTransactionByAccount(String account) {
        long start = System.currentTimeMillis();
        if (!transactionByAccount.containsKey(account)) {
            transactionByAccount.put(account, new ArrayList<Transaction>());
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getTransactionByAccount {" + (end - start) + "}");

        return transactionByAccount.get(account);
    }

    private List<Transaction> getTransactionByAccount(String account, DateTime period) {
        long start = System.currentTimeMillis();
        if (!transactionByAccount.containsKey(account)) {
            transactionByAccount.put(account, new ArrayList<Transaction>());
        }

        List<Transaction> result = new ArrayList<Transaction>();
        for (Transaction t : transactionByAccount.get(account)) {
            if (t.getDate().getMonthOfYear() == period.getMonthOfYear() &&
                    t.getDate().getYear() == period.getYear()) {
                result.add(t);
            }
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getTransactionByAccountDate {" + (end - start) + "}");
        return result;
    }

    public List<Transaction> getTransactionByBudget(String budget) {
        long start = System.currentTimeMillis();
        if (!transactionByBudget.containsKey(budget)) {
            transactionByBudget.put(budget, new ArrayList<Transaction>());
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getTransactionByBudget {" + (end - start) + "}");
        return transactionByBudget.get(budget);
    }

    private List<Transaction> getTransactionByBudget(String budget,
                                                          DateTime startDate, DateTime endDate) {
        long start  = System.currentTimeMillis();
        if (!transactionByBudget.containsKey(budget)) {
            transactionByBudget.put(budget, new ArrayList<Transaction>());
        }

        List<Transaction> result = new ArrayList<Transaction>();
        for (Transaction t : transactionByBudget.get(budget)) {
            if(Utils.isAfterOrEqual(t.getDate(), startDate) && Utils.isBefore(t.getDate(), endDate)){
                result.add(t);
            }
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getTransactionByBudgetDate {" + (end - start) + "}");
        return result;
    }

    public List<Transaction> getOutTransactionsExcludingFees() {
        if (!started) {
            return Collections.emptyList();
        }

        long start = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<>();
        for (Transaction t  : outTransaction) {
            if (!t.getType().equals(TransactionType.Fee)) {
                transactions.add(t);
            }
        }

        long sorting = System.currentTimeMillis();
        Collections.sort(transactions, new TransactionByDateComparator());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES", "BUDGET_MANAGER getOutTransactionsExcludingFees (" + transactions.size() + ") " +
                " get {" + (sorting - start) + "}" +
                " sort {" + (end - sorting) + "} tot {" + (end - start) + "}");
        return transactions;
    }

    public List<Transaction> getInTransactions() {
        if (!started) {
            return Collections.emptyList();
        }

        long start = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<Transaction>(inTransaction);

        long sorting = System.currentTimeMillis();
        Collections.sort(transactions, new TransactionByDateComparator());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES", "BUDGET_MANAGER getInTransactions (" + transactions.size() + ") " +
                " get {" + (sorting - start) + "}" +
                " sort {" + (end - sorting) + "} tot {" + (end - start) + "}");
        return transactions;
    }

    public List<CompoundedTransferTransaction> getTransferTransactions() {
        if (!started) {
            return Collections.emptyList();
        }

        long start = System.currentTimeMillis();
        List<Transaction> transactions = new ArrayList<Transaction>(transferTransaction);

        long sorting = System.currentTimeMillis();
        Collections.sort(transactions, new TransactionByDateComparator());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES", "BUDGET_MANAGER getTransferTransactions (" + transactions.size() + ") " +
                " get {" + (sorting - start) + "}" +
                " sort {" + (end - sorting) + "} tot {" + (end - start) + "}");

        List<CompoundedTransferTransaction> compoundedTransactions =
                new ArrayList<CompoundedTransferTransaction>(transactions.size() / 2);

        for (Transaction t : transactions) {
            if (t.getDirection().equals(TransactionDirection.Out)) {
                Transaction t2 = getTransactionById(t.getLinkedTransactionId());
                if (!t.getFeeTransactionId().isEmpty()) {
                    Transaction tFee = getTransactionById(t.getFeeTransactionId());
                    compoundedTransactions.add(new CompoundedTransferTransaction(t, t2, tFee));
                } else {
                    compoundedTransactions.add(new CompoundedTransferTransaction(t, t2));
                }
            }
        }

        return compoundedTransactions;
    }

    static public Transaction createRecurrentCopy(Transaction transaction, DateTime date) {
        Transaction t = new Transaction(transaction.getId(),
                transaction.getDescription(),
                transaction.getDirection(),
                transaction.getType(),
                transaction.getAccountId(),
                transaction.getBudgetId(),
                transaction.getValue(),
                date);

        t.setLinkedTransactionId(transaction.getLinkedTransactionId());
        t.setRecurrent(true);
        t.setAutoGenerated(true);
        return t;
    }

    static public Collection<Transaction> explodeRecurrentTransaction(Transaction transaction,
                                                                DateTime currentDate) {
        //TODO support transaction with repetition num instead of end-date and viceversa
        //TODO support transaction with no end-date or repetition num
        long start = System.currentTimeMillis();

        int iterationNum = 1;
        DateTime newDate = getNextTransactionDate(transaction, iterationNum);
        Collection<Transaction> transactions = new ArrayList<Transaction>();

        while (Utils.isBeforeOrEqual(newDate, transaction.getEndDate()) &&
                Utils.isBeforeOrEqual(newDate, currentDate)) {
            transactions.add(TransactionManager.createRecurrentCopy(transaction, newDate));
            iterationNum++;
            newDate = getNextTransactionDate(transaction, iterationNum);
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER explodeTransaction {" + (end - start) + "}");

        return transactions;
    }

    static private DateTime getNextTransactionDate(Transaction transaction, int step) {
        DateTime start = transaction.getDate();
        switch (transaction.getFrequencyUnit()) {
            case Day:
                return start.plusDays(transaction.getFrequency() * step);
            case Week:
                return  start.plusWeeks(transaction.getFrequency() * step);
            case Month:
                return start.plusMonths(transaction.getFrequency() * step);
            case Year:
                return  start.plusYears(transaction.getFrequency() * step);
        }
        return null;
    }

    private DateTime accountPeriodDate = null;
    private BigDecimal accountPeriodBalance = null;

    public void resetGetAccountNextPeriodTransactions(DateTime date) {
        accountPeriodDate = date;
        accountPeriodBalance = BigDecimal.ZERO;
    }

    public List<Transaction> getAccountNextPeriodTransactions(String account, boolean finished) {
        long start = System.currentTimeMillis();

        List<Transaction> transactions = new ArrayList<Transaction>(
                getTransactionByAccount(account, accountPeriodDate)
        );

        BigDecimal in = BigDecimal.ZERO;
        BigDecimal out = BigDecimal.ZERO;
        List<Transaction> resultTransactions = new ArrayList<Transaction>();

        for(Transaction transaction : transactions) {
            if (transaction.getDirection().equals(TransactionDirection.In)) {
                in = in.add(transaction.getValue());
            }
            if (transaction.getDirection().equals(TransactionDirection.Out)) {
                out = out.add(transaction.getValue());
            }
            resultTransactions.add(transaction);
        }

        if(!resultTransactions.isEmpty() && Utils.isBefore(firstTransactionDate, accountPeriodDate)) {
            AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER().getAccountInfo(account);
            BigDecimal balance = accountInfo.balance.subtract(accountPeriodBalance);
            BigDecimal diff = in.subtract(out);
            accountPeriodBalance = accountPeriodBalance.add(diff);

            SummaryTransaction summary = new SummaryTransaction(in, out, diff, balance,  accountPeriodDate);
            resultTransactions.add(0, summary);
        }

        accountPeriodDate = accountPeriodDate.minusMonths(1);
        if (!Utils.isAfterOrEqual(firstTransactionDate, accountPeriodDate)) {
            finished = true;
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getAccountNextPeriod {" + (end - start) + "}");

        return resultTransactions;
    }

    private DateTime budgetPeriodDate = null;

    public void resetGetBudgetNextPeriodTransactions(DateTime date) {
        budgetPeriodDate = date;
    }

    public List<Transaction> getBudgetNextPeriodTransactions(String budget, boolean finished) {
        long start = System.currentTimeMillis();

        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER().getBudgetInfo(budget);
        Pair<DateTime, DateTime> periodStartEnd = budgetInfo.getPeriodStartEnd(budgetPeriodDate);
        DateTime periodStart = periodStartEnd.first;
        DateTime periodEnd = periodStartEnd.second;

        if(periodStart.isEqual(periodEnd)) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new ArrayList<Transaction>(
                getTransactionByBudget(budget, periodStart, periodEnd)
        );

        BigDecimal periodIn = BigDecimal.ZERO;
        BigDecimal periodOut = BigDecimal.ZERO;
        List<Transaction> resultTransactions = new ArrayList<Transaction>();

        for (Transaction transaction : transactions) {
            if (transaction.getDirection().equals(TransactionDirection.Out)) {
                periodOut = periodOut.add(transaction.getValue());
            }
            if (transaction.getDirection().equals(TransactionDirection.In)) {
                periodIn = periodIn.add(transaction.getValue());
            }
            resultTransactions.add(transaction);
        }

        if(!resultTransactions.isEmpty() || Utils.isBefore(firstTransactionDate, periodStart)) {
            SummaryTransaction summary = new SummaryTransaction(periodIn, periodOut, BigDecimal.ZERO,
                    periodIn.subtract(periodOut),  budgetPeriodDate);

            resultTransactions.add(0, summary);
        }

        budgetPeriodDate = periodStart.minusDays(1);
        if (resultTransactions.isEmpty()) {
            finished = true;
        }

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "TRANSACTION_MANAGER getBudgetNextPeriod {" + (end - start) + "}");

        return resultTransactions;
    }

    public SummaryTransaction getBudgetNextPeriodTransactionsSummary(String budget) {
        boolean finished = false;
        List<Transaction> transactions = getBudgetNextPeriodTransactions(budget, finished);
        for (Transaction transaction : transactions) {
            if (transaction instanceof SummaryTransaction) {
                return (SummaryTransaction) transaction;
            }
        }
        return null;
    }

    public SummaryTransaction getAccountNextPeriodTransactionsSummary(String account) {
        boolean finished = false;
        List<Transaction> transactions = getAccountNextPeriodTransactions(account, finished);
        for (Transaction transaction : transactions) {
            if (transaction instanceof SummaryTransaction) {
                return (SummaryTransaction) transaction;
            }
        }
        return null;
    }

    public String[] getDescriptionsArray() {
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return descriptionsArray.toArray(new String[0]);
    }

}
