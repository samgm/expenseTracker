package com.antso.expensesmanager.transactions;

import android.content.Context;
import android.util.Pair;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.TransactionByDateComparator;
import com.antso.expensesmanager.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public enum TransactionManager {
        TRANSACTION_MANAGER;

    private DatabaseHelper dbHelper = null;
    private Set<String> descriptionsArray = null;

    private String revenueSummaryDescription = "";
    private String expenseSummaryDescription = "";
    private String totalSummaryDescription = "";

    private TransactionManager() {
    }

    public void start(Context context) {
        revenueSummaryDescription = context.getText(R.string.revenue_label).toString();
        expenseSummaryDescription = context.getText(R.string.expense_label).toString();
        totalSummaryDescription = context.getText(R.string.total_label).toString();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }

        Collection<Transaction> result = dbHelper.getTransactions();
        if(descriptionsArray == null) {
            descriptionsArray = new HashSet<String>();
            for (Transaction t : result) {
                descriptionsArray.add(t.getDescription());
            }
        }
    }

    public void stop() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void insertTransaction(Transaction transaction) {
        dbHelper.insertTransactions(transaction);

        descriptionsArray.add(transaction.getDescription());

        AccountManager.ACCOUNT_MANAGER.onTransactionAdded(transaction);
        BudgetManager.BUDGET_MANAGER.onTransactionAdded(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        if(transaction.getLinkedTransactionId() != null &&
                !transaction.getLinkedTransactionId().isEmpty()) {
            dbHelper.deleteTransaction(transaction.getLinkedTransactionId());
        }
        dbHelper.deleteTransaction(transaction.getId());

        AccountManager.ACCOUNT_MANAGER.onTransactionDeleted(transaction);
        BudgetManager.BUDGET_MANAGER.onTransactionDeleted(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        Transaction oldTransaction = dbHelper.getTransactionsById(transaction.getId());
        dbHelper.updateTransaction(transaction);

        descriptionsArray.add(transaction.getDescription());

        AccountManager.ACCOUNT_MANAGER.onTransactionDeleted(oldTransaction);
        BudgetManager.BUDGET_MANAGER.onTransactionDeleted(oldTransaction);
        AccountManager.ACCOUNT_MANAGER.onTransactionAdded(transaction);
        BudgetManager.BUDGET_MANAGER.onTransactionAdded(transaction);
    }

    public void replaceAccount(String fromAccountId, String toAccountId) {
        Collection<Transaction> transactions = dbHelper.getTransactionsByAccount(fromAccountId);
        for (Transaction transaction : transactions) {
            dbHelper.deleteTransaction(transaction.getId());
            AccountManager.ACCOUNT_MANAGER.onTransactionDeleted(transaction);
            transaction.setAccount(toAccountId);
            AccountManager.ACCOUNT_MANAGER.onTransactionAdded(transaction);
            dbHelper.insertTransactions(transaction);
        }
    }

    public void replaceBudget(String fromBudgetId, String toBudgetId) {
        Collection<Transaction> transactions = dbHelper.getTransactionsByBudget(fromBudgetId);
        for (Transaction transaction : transactions) {
            dbHelper.deleteTransaction(transaction.getId());
            BudgetManager.BUDGET_MANAGER.onTransactionDeleted(transaction);
            transaction.setBudget(toBudgetId);
            BudgetManager.BUDGET_MANAGER.onTransactionAdded(transaction);
            dbHelper.insertTransactions(transaction);
        }
    }

    public void removeTransactionByAccount(String accountId) {
        Collection<Transaction> transactions = dbHelper.getTransactionsByAccount(accountId);
        for (Transaction transaction : transactions) {
            dbHelper.deleteTransaction(transaction.getId());
            AccountManager.ACCOUNT_MANAGER.onTransactionDeleted(transaction);
            BudgetManager.BUDGET_MANAGER.onTransactionDeleted(transaction);
        }
    }

    public Transaction getTransactionById(String id) {
        return dbHelper.getTransactionsById(id);
    }

    public Collection<Transaction> getTransactions(TransactionDirection direction, boolean noTransfer) {
        return dbHelper.getTransactions(direction, noTransfer);
    }

    public Collection<Transaction> getTransactions(TransactionType type) {
        return dbHelper.getTransactions(type);
    }

    public List<Transaction> getOutTransactions() {
        List<Transaction> transactions = new ArrayList<Transaction>(
                getTransactions(TransactionDirection.Out, true)
        );
        Collections.sort(transactions, new TransactionByDateComparator());
        return transactions;
    }

    public List<Transaction> getInTransactions() {
        List<Transaction> transactions = new ArrayList<Transaction>(
                getTransactions(TransactionDirection.In, true)
        );
        Collections.sort(transactions, new TransactionByDateComparator());
        return transactions;
    }

    public List<Pair<Transaction, Transaction>> getTransferTransactions() {
        List<Transaction> transactions = new ArrayList<Transaction>(
                getTransactions(TransactionType.Transfer)
        );
        Collections.sort(transactions, new TransactionByDateComparator());

        List<Pair<Transaction, Transaction>> pairedTransactions =
                new ArrayList<Pair<Transaction, Transaction>>(transactions.size() / 2);

        Iterator<Transaction> iterator = transactions.iterator();
        while (iterator.hasNext()) {
            Transaction t1 = iterator.next();
            Transaction t2 = iterator.next();
            pairedTransactions.add(new Pair<Transaction, Transaction>(t1, t2));
        }

        return pairedTransactions;
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
        t.setAutoGenerated(true);
        return t;
    }

    static public Collection<Transaction> explodeRecurrentTransaction(Transaction transaction,
                                                                DateTime currentDate) {
        //TODO support transaction with repetition num instead of end-date and viceversa
        //TODO support transaction with no end-date or repetition num

        int iterationNum = 1;
        DateTime newDate = getNextTransactionDate(transaction, iterationNum);
        Collection<Transaction> transactions = new ArrayList<Transaction>();

        while (Utils.isBeforeOrEqual(newDate, transaction.getEndDate()) &&
                Utils.isBeforeOrEqual(newDate, currentDate)) {
            transactions.add(TransactionManager.createRecurrentCopy(transaction, newDate));
            iterationNum++;
            newDate = getNextTransactionDate(transaction, iterationNum);
        }

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

    public void resetGetAccountNextPeriodTransactions(DateTime date) {
        accountPeriodDate = date;
    }

    public List<Transaction> getAccountNextPeriodTransactions(String account) {
        List<Transaction> transactions = new ArrayList<Transaction>(
                dbHelper.getTransactionsByAccount(account)
        );

        Collection<Transaction> exploded = new ArrayList<Transaction>();
        for(Transaction transaction : transactions) {
            if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
                exploded.addAll(explodeRecurrentTransaction(transaction, DateTime.now()));
            }
        }
        transactions.addAll(exploded);

        Collections.sort(transactions, new TransactionByDateComparator());

        BigDecimal in = BigDecimal.ZERO;
        BigDecimal out = BigDecimal.ZERO;
        List<Transaction> resultTransactions = new ArrayList<Transaction>();

        for(Transaction transaction : transactions) {
            if (transaction.getDate().getMonthOfYear() == accountPeriodDate.getMonthOfYear() &&
                    transaction.getDate().getYear() == accountPeriodDate.getYear()) {
                if (transaction.getDirection().equals(TransactionDirection.In)) {
                    in = in.add(transaction.getValue());
                }
                if (transaction.getDirection().equals(TransactionDirection.Out)) {
                    out = out.add(transaction.getValue());
                }
                resultTransactions.add(transaction);
            }
        }

        if(!resultTransactions.isEmpty()) {
            Transaction tin = new Transaction("RevenueId", revenueSummaryDescription, TransactionDirection.In,
                    TransactionType.Summary, "", "", in, accountPeriodDate);
            Transaction tout = new Transaction("ExpensesId", expenseSummaryDescription, TransactionDirection.Out,
                    TransactionType.Summary, "", "", out, accountPeriodDate);

            AccountManager.AccountInfo accountInfo = AccountManager.ACCOUNT_MANAGER.getAccountInfo(account);
            BigDecimal balance = accountInfo.getByDateBalance(accountPeriodDate);

            TransactionDirection direction;
            if(balance.compareTo(BigDecimal.ZERO) > 0) {
                direction = TransactionDirection.In;
            } else {
                direction = TransactionDirection.Out;
            }

            Transaction ttot = new Transaction("TotalId", totalSummaryDescription, direction,
                    TransactionType.Summary, "", "", balance, accountPeriodDate);

            resultTransactions.add(0, ttot);
            resultTransactions.add(0, tout);
            resultTransactions.add(0, tin);
        }

        accountPeriodDate = accountPeriodDate.minusMonths(1);
        return resultTransactions;
    }

    private DateTime budgetPeriodDate = null;

    public void resetGetBudgetNextPeriodTransactions(DateTime date) {
        budgetPeriodDate = date;
    }

    public Collection<Transaction> getBudgetNextPeriodTransactions(String budget) {
        List<Transaction> transactions = new ArrayList<Transaction>(
                dbHelper.getTransactionsByBudget(budget)
        );

        Collection<Transaction> exploded = new ArrayList<Transaction>();
        for(Transaction transaction : transactions) {
            if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
                exploded.addAll(explodeRecurrentTransaction(transaction, DateTime.now()));
            }
        }
        transactions.addAll(exploded);

        Collections.sort(transactions, new TransactionByDateComparator());

        BigDecimal periodIn = BigDecimal.ZERO;
        BigDecimal periodOut = BigDecimal.ZERO;
        List<Transaction> resultTransactions = new ArrayList<Transaction>();
        BudgetManager.BudgetInfo budgetInfo = BudgetManager.BUDGET_MANAGER.getBudgetInfo(budget);

        Pair<DateTime, DateTime> periodStartEnd = budgetInfo.getPeriodStartEnd(budgetPeriodDate);
        DateTime periodStart = periodStartEnd.first;
        DateTime periodEnd = periodStartEnd.second;

        for (Transaction transaction : transactions) {
            if(transaction.getDate().isAfter(periodStart) &&
                    transaction.getDate().isBefore(periodEnd)){
                if (transaction.getDirection().equals(TransactionDirection.Out)) {
                    periodOut = periodOut.add(transaction.getValue());
                }
                if (transaction.getDirection().equals(TransactionDirection.In)) {
                    periodIn = periodIn.add(transaction.getValue());
                }
                resultTransactions.add(transaction);
            }
        }


        if(!resultTransactions.isEmpty()) {
            Transaction tin = new Transaction("RevenueId", revenueSummaryDescription, TransactionDirection.In,
                    TransactionType.Summary, "", "", periodIn, budgetPeriodDate);
            Transaction tout = new Transaction("ExpensesId", expenseSummaryDescription, TransactionDirection.Out,
                    TransactionType.Summary, "", "", periodOut, budgetPeriodDate);

            BigDecimal periodBalance = periodIn.subtract(periodOut);
            TransactionDirection direction;
            if(periodBalance.compareTo(BigDecimal.ZERO) > 0) {
                direction = TransactionDirection.In;
            } else {
                direction = TransactionDirection.Out;
            }

            Transaction ttot = new Transaction("TotalId", totalSummaryDescription, direction,
                    TransactionType.Summary, "", "", periodBalance, budgetPeriodDate);

            resultTransactions.add(0, ttot);
            resultTransactions.add(0, tout);
            resultTransactions.add(0, tin);
        }

        budgetPeriodDate = periodStart;
        return resultTransactions;
    }

    public String[] getDescriptionsArray() {
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return descriptionsArray.toArray(new String[0]);
    }

}
