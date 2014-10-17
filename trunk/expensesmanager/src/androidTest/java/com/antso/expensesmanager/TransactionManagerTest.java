package com.antso.expensesmanager;

import android.test.AndroidTestCase;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.SummaryTransaction;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.Utils;

import java.math.BigDecimal;
import java.util.List;

public class TransactionManagerTest extends AndroidTestCase {
    private Transaction badFirst;
    private Transaction goodFirst;
    private Transaction goodSecond;
    private Transaction goodThird;
    private Transaction goodRecurrentFirstAndSecond;
    private Transaction goodRecurrentSecondAndThird;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        dbHelper.clearDatabase();

        TransactionManager.TRANSACTION_MANAGER().start(getContext());
        BudgetManager.BUDGET_MANAGER().start(getContext());
        AccountManager.ACCOUNT_MANAGER().start(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        AccountManager.ACCOUNT_MANAGER().stop();
        BudgetManager.BUDGET_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().stop();
    }

    private void createAndAddTransactionsForBudgetTest() {
        badFirst = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141023));
        goodFirst = new Transaction("t2", "t2", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        goodSecond = new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141012));
        goodThird = new Transaction("t4", "t4", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141003));

        goodRecurrentFirstAndSecond = new Transaction("t5", "t5", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141013));
        goodRecurrentSecondAndThird = new Transaction("t6", "t6", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141002));
        goodRecurrentFirstAndSecond.setRecurrent(true);
        goodRecurrentSecondAndThird.setRecurrent(true);
        goodRecurrentFirstAndSecond.setFrequency(1);
        goodRecurrentFirstAndSecond.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentFirstAndSecond.setEndDate(Utils.yyyyMMddToDate(20141021));
        goodRecurrentSecondAndThird.setFrequency(1);
        goodRecurrentSecondAndThird.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentSecondAndThird.setEndDate(Utils.yyyyMMddToDate(20141009));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(badFirst);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodFirst);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodSecond);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodThird);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrentFirstAndSecond);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrentSecondAndThird);
    }

    private void createAndAddTransactionsForAccountTest() {
        badFirst = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141123));
        goodFirst = new Transaction("t2", "t2", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        goodSecond = new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140912));
        goodThird = new Transaction("t4", "t4", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140803));

        goodRecurrentFirstAndSecond = new Transaction("t5", "t5", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140913));
        goodRecurrentSecondAndThird = new Transaction("t6", "t6", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20140802));
        goodRecurrentFirstAndSecond.setRecurrent(true);
        goodRecurrentSecondAndThird.setRecurrent(true);
        goodRecurrentFirstAndSecond.setFrequency(1);
        goodRecurrentFirstAndSecond.setFrequencyUnit(TimeUnit.Month);
        goodRecurrentFirstAndSecond.setEndDate(Utils.yyyyMMddToDate(20141121));
        goodRecurrentSecondAndThird.setFrequency(1);
        goodRecurrentSecondAndThird.setFrequencyUnit(TimeUnit.Month);
        goodRecurrentSecondAndThird.setEndDate(Utils.yyyyMMddToDate(20141009));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(badFirst);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodFirst);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodSecond);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodThird);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrentFirstAndSecond);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrentSecondAndThird);
    }

    public void testGetBudgetNextPeriodTransactions() {
        // Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Week, Utils.yyyyMMddToDate(20141002)); //16-Oct -> 22-Oct
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForBudgetTest();

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetBudgetNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then1
        assertTrue(period.size() == 1 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.ONE) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.ONE) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then2
        assertTrue(period.size() == 3 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-2)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then3
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-1)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then4
        assertTrue(period.size() == 0 + 0);

    }

    public void testGetBudgetNextPeriodTransactionsAfterDelete() {
        // Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Week, Utils.yyyyMMddToDate(20141002)); //16-Oct -> 22-Oct
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForBudgetTest();
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodSecond);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodRecurrentFirstAndSecond);

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetBudgetNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then1
        assertTrue(period.size() == 1 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.ONE) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.ONE) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then2
        assertTrue(period.size() == 1 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-2)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then3
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-1)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then4
        assertTrue(period.size() == 0 + 0);
    }

    public void testGetBudgetNextPeriodTransactionsAfterStart() {
        // Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Week, Utils.yyyyMMddToDate(20141002)); //16-Oct -> 22-Oct
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForBudgetTest();

        BudgetManager.BUDGET_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().start(getContext());
        BudgetManager.BUDGET_MANAGER().start(getContext());

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetBudgetNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then1
        assertTrue(period.size() == 1 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.ONE) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.ONE) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then2
        assertTrue(period.size() == 3 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-2)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then3
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(-1)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getBudgetNextPeriodTransactions("b1");

        // Then4
        assertTrue(period.size() == 0 + 0);
    }

    public void testGetAccountNextPeriodTransactions() {
        // Given
        Account a1 = new Account("a1", "a1", BigDecimal.TEN, 0);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForAccountTest();

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetAccountNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then1
        assertTrue(period.size() == 3 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(7)) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then2
        assertTrue(period.size() == 3 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(-2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(7)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then3
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(-1)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(9)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then4
        assertTrue(period.size() == 0 + 0);
    }

    public void testGetAccountNextPeriodTransactionsAfterDelete() {
        // Given
        Account a1 = new Account("a1", "a1", BigDecimal.TEN, 0);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForAccountTest();
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodFirst);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodRecurrentSecondAndThird);

//        goodSecond = new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140912));
//        goodThird = new Transaction("t4", "t4", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140803));
//
//        goodRecurrentFirstAndSecond = new Transaction("t5", "t5", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20140913));
//        goodRecurrentSecondAndThird = new Transaction("t6", "t6", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20140802));
//        goodRecurrentFirstAndSecond.setFrequencyUnit(TimeUnit.Month);
//        goodRecurrentFirstAndSecond.setEndDate(Utils.yyyyMMddToDate(20141121));
//        goodRecurrentSecondAndThird.setFrequencyUnit(TimeUnit.Month);
//        goodRecurrentSecondAndThird.setEndDate(Utils.yyyyMMddToDate(20141009));

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetAccountNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then1
//        assertTrue(period.size() == 1 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(12)) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then2
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(11)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then3
        assertTrue(period.size() == 1 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(11)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then4
        assertTrue(period.size() == 0 + 0);
    }

    public void testGetAccountNextPeriodTransactionsAfterStart() {
        // Given
        Account a1 = new Account("a1", "a1", BigDecimal.TEN, 0);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForAccountTest();

        AccountManager.ACCOUNT_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().start(getContext());
        AccountManager.ACCOUNT_MANAGER().start(getContext());

        // When1
        TransactionManager.TRANSACTION_MANAGER().resetGetAccountNextPeriodTransactions(Utils.yyyyMMddToDate(20141016));
        List<Transaction> period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then1
        assertTrue(period.size() == 3 + 1);
        SummaryTransaction summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(7)) == 0);

        // When2
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then2
        assertTrue(period.size() == 3 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(-2)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(7)) == 0);

        //When3
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then3
        assertTrue(period.size() == 2 + 1);
        summary = (SummaryTransaction)period.get(0);
        assertTrue(summary.getType().equals(TransactionType.Summary));
        assertTrue(summary.getValueIn().compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(summary.getValueOut().compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(summary.getValueDiff().compareTo(BigDecimal.valueOf(-1)) == 0);
        assertTrue(summary.getBalance().compareTo(BigDecimal.valueOf(9)) == 0);

        // When4
        period = TransactionManager.TRANSACTION_MANAGER().getAccountNextPeriodTransactions("a1");

        // Then4
        assertTrue(period.size() == 0 + 0);

    }

    // ripetere i test sopra
    //      con periodi di 1 giorno
    //      con delete di transfer
}