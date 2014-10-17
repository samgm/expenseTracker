package com.antso.expensesmanager;

import android.test.AndroidTestCase;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.Utils;

import java.math.BigDecimal;

public class BudgetManagerTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        dbHelper.clearDatabase();

        TransactionManager.TRANSACTION_MANAGER().start(getContext());
        AccountManager.ACCOUNT_MANAGER().start(getContext());
        BudgetManager.BUDGET_MANAGER().start(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        BudgetManager.BUDGET_MANAGER().stop();
        AccountManager.ACCOUNT_MANAGER().stop();
        TransactionManager.TRANSACTION_MANAGER().stop();
    }

    private Transaction createTransaction(String id, String account, String budget, TransactionDirection dir, TransactionType type, BigDecimal value, int date) {
        return new Transaction(id, id, dir, type, account, budget, value, Utils.yyyyMMddToDate(date));
    }

    private Transaction createTransaction(String id, String account, String budget, TransactionDirection dir, TransactionType type,BigDecimal value, int date, int freqNum, TimeUnit freqUnit, int end) {
        Transaction t = new Transaction(id, id, dir, type, account, budget, value, Utils.yyyyMMddToDate(date));
        t.setRecurrent(true);
        t.setFrequency(freqNum);
        t.setFrequencyUnit(freqUnit);
        t.setEndDate(Utils.yyyyMMddToDate(end));
        return t;
    }

//    budget in out balance
//    1 giving some existing transaction (also with transaction on the edge dates)
//    2 after add,
//    3 after delete,
//    4 after edit value,
//            5 after edit budget,
//            6 after delete budged (moving to another)
//    7 repeat 1-6 with recurrent transactions

    // get NextBudgetPeriodTransactions NextAccountPeriodTransactions

    public void testBudgetValuesForSingleTransactionsWhenAddAndDel() {
        //Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        // When
        Transaction goodIn = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single,
                "a1", "b1", BigDecimal.TEN, Utils.yyyyMMddToDate(20141016));
        Transaction wrongBudget = new Transaction("t2", "t2", TransactionDirection.Out, TransactionType.Single,
                "a1", "b2", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        Transaction goodOut =  new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single,
                "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        Transaction wrongDate1 =  new Transaction("t4", "t4", TransactionDirection.Out, TransactionType.Single,
                "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141116));
        Transaction wrongDate2 =  new Transaction("t5", "t5", TransactionDirection.Out, TransactionType.Single,
                "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141015));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongBudget);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate2);        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate1);


        BudgetManager.BUDGET_MANAGER().stop();
        BudgetManager.BUDGET_MANAGER().start(getContext());

        // Then
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.TEN) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongBudget);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate1);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate2);

        // Then
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.ZERO) == 0);

        // Finally
        BudgetManager.BUDGET_MANAGER().removeBudget(info.budget);
    }

    public void testBudgetValuesForSingleTransactionsWhenEdit() {

    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenAddAndDel() {

    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEdit() {

    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEditBudgetValue() {

    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEditBudgetPeriod() {

    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsDeleteBudget() {

    }

}