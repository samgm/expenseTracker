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

public class AccountManagerTest extends AndroidTestCase {

//    Transaction goodIn;
//    Transaction wrongBudget;
//    Transaction goodOut;
//    Transaction wrongDate1;
//    Transaction wrongDate2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141016));

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

    private void createAndAddTransactionsForTest() {
//        goodIn = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.TEN, Utils.yyyyMMddToDate(20141016));
//        wrongBudget = new Transaction("t2", "t2", TransactionDirection.Out, TransactionType.Single, "a1", "b2", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
//        goodOut =  new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
//        wrongDate1 =  new Transaction("t4", "t4", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141116));
//        wrongDate2 =  new Transaction("t5", "t5", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141015));
//
//        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodIn);
//        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongBudget);
//        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodOut);
//        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate2);
//        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate1);
    }

    private void delTransactionsForTest() {
//        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodIn);
//        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongBudget);
//        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodOut);
//        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate1);
//        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate2);
    }

    public void testAccountValuesForSingleTransactionsWhenAddAndDel() {
//        //Given
//        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
//                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
//        BudgetManager.BUDGET_MANAGER().insertBudget(b1);
//
//        // When 1
//        createAndAddTransactionsForTest();
//
//        // Then 1
//        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.TEN) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(9)) == 0);
//
//        // When 2
//        BudgetManager.BUDGET_MANAGER().stop();
//        BudgetManager.BUDGET_MANAGER().start(getContext());
//
//        // Then 2
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.TEN) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(9)) == 0);
//
//        // When 3
//        delTransactionsForTest();
//
//        // Then 3
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.ZERO) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ZERO) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.ZERO) == 0);
    }

    public void testAccountValuesForSingleTransactionsWhenEdit() {
//        //Given
//        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
//                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
//        BudgetManager.BUDGET_MANAGER().insertBudget(b1);
//
//        createAndAddTransactionsForTest();
//
//        // When 1
//        Transaction goodChangedValue = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
//        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedValue);
//
//        // Then 1
//        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(5)) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(4)) == 0);
//
//        // When 2
//        Transaction goodChangedBudget = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b2", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
//        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedBudget);
//
//        // Then 2
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);
//
//        // When 3
//        Transaction goodChangedDate1 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141015));
//        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate1);
//
//        // Then 3
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);
//
//        // When 4
//        Transaction goodChangedDate2 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141116));
//        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate2);
//
//        // Then 4
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);
//
//        // When 5
//        BudgetManager.BUDGET_MANAGER().stop();
//        BudgetManager.BUDGET_MANAGER().start(getContext());
//
//        // Then 5
//        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
//        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
//        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
//        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);
    }

//    public void testAccountValuesForSingleAndRecurrentTransactionsWhenAddAndDel() {
//
//    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenEdit() {
        //goodChangedToRecurrent back to normal

    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenEditBudgetValue() {

    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenEditBudgetPeriod() {

    }

    public void testAccountValuesForSingleAndRecurrentTransactionsDeleteBudget() {

    }

    //    budget in out balance
//    1 giving some existing transaction (also with transaction on the edge dates)
//    2 after add,
//    3 after delete,
//    4 after edit value,
//            5 after edit budget,
//            6 after delete budged (moving to another)
//    7 repeat 1-6 with recurrent transactions

}