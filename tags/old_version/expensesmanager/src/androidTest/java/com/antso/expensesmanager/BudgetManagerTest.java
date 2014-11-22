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

    Transaction goodIn;
    Transaction wrongBudget;
    Transaction goodOut;
    Transaction wrongDate1;
    Transaction wrongDate2;

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
        goodIn = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.TEN, Utils.yyyyMMddToDate(20141016));
        wrongBudget = new Transaction("t2", "t2", TransactionDirection.Out, TransactionType.Single, "a1", "b2", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        goodOut =  new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141016));
        wrongDate1 =  new Transaction("t4", "t4", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141116));
        wrongDate2 =  new Transaction("t5", "t5", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.ONE, Utils.yyyyMMddToDate(20141015));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongBudget);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate2);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate1);
    }

    private void delTransactionsForTest() {
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongBudget);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate1);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate2);
    }

    public void testBudgetValuesForSingleTransactionsWhenAddAndDel() {
        //Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        // When 1
        createAndAddTransactionsForTest();

        // Then 1
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.TEN) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When 2
        BudgetManager.BUDGET_MANAGER().stop();
        BudgetManager.BUDGET_MANAGER().start(getContext());

        // Then 2
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.TEN) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When 3
        delTransactionsForTest();

        // Then 3
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.ZERO) == 0);
    }

    public void testBudgetValuesForSingleTransactionsWhenEdit() {
        //Given
        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForTest();

        // When 1
        Transaction goodChangedValue = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedValue);

        // Then 1
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(5)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(4)) == 0);

        // When 2
        Transaction goodChangedBudget = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b2", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedBudget);

        // Then 2
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);

        // When 3
        Transaction goodChangedDate1 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141015));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate1);

        // Then 3
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);

        // When 4
        Transaction goodChangedDate2 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141116));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate2);

        // Then 4
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);

        // When 5
        BudgetManager.BUDGET_MANAGER().stop();
        BudgetManager.BUDGET_MANAGER().start(getContext());

        // Then 5
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ONE) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(-1)) == 0);
    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenAddAndDel() {
        //Given
        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141112));

        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        // When 1
        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141014));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(1);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // Then 1
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(18)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(17)) == 0);

        // When 2
        BudgetManager.BUDGET_MANAGER().stop();
        BudgetManager.BUDGET_MANAGER().start(getContext());

        // Then 2
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(18)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(17)) == 0);

        // When 3
        delTransactionsForTest();
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodRecurrent);

        // Then 3
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.ZERO) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.ZERO) == 0);
    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEdit() {
        //Given
        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141112));

        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        // When 1
        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141014));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(1);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // Then 1
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(18)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(17)) == 0);

        // When 2
        Transaction goodRecurrentEdit1 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141014));
        goodRecurrentEdit1.setRecurrent(true);
        goodRecurrentEdit1.setFrequency(1);
        goodRecurrentEdit1.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit1.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit1);

        // Then 2
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(30)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(29)) == 0);

        // When 3
        Transaction goodRecurrentEdit2 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141028));
        goodRecurrentEdit2.setRecurrent(true);
        goodRecurrentEdit2.setFrequency(1);
        goodRecurrentEdit2.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit2.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit2);

        // Then 3
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(25)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(24)) == 0);

        // When 4
        Transaction goodRecurrentEdit3 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141028));
        goodRecurrentEdit3.setRecurrent(true);
        goodRecurrentEdit3.setFrequency(2);
        goodRecurrentEdit3.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit3.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit3);

        // Then 4
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(20)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(19)) == 0);

        // When 5
        Transaction goodRecurrentEdit4 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141028));
        goodRecurrentEdit4.setRecurrent(false);
        goodRecurrentEdit4.setFrequency(2);
        goodRecurrentEdit4.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit4.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit4);

        // Then 5
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(15)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(14)) == 0);
    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEditBudgetValue() {
        //Given
        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141112));

        Budget b1 = new Budget("b1", "b1", BigDecimal.TEN, 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141014));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(1);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When
        Budget b1Edit = new Budget("b1", "b1", BigDecimal.valueOf(15), 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));
        BudgetManager.BUDGET_MANAGER().updateBudget(b1Edit);

        // Then
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(18)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(17)) == 0);
    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsWhenEditBudgetPeriod() {
        //Given
        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141112));

        Budget b1 = new Budget("b1", "b1", BigDecimal.valueOf(10), 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141014));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(1);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When 1
        Budget b1EditPeriod = new Budget("b1", "b1", BigDecimal.valueOf(10), 0,
                2, TimeUnit.Week, Utils.yyyyMMddToDate(20141016));
        BudgetManager.BUDGET_MANAGER().updateBudget(b1EditPeriod);

        // Then 1
        BudgetManager.BudgetInfo info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(4)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(4)) == 0);

        // When 2
        Budget b1EditDate = new Budget("b1", "b1", BigDecimal.valueOf(10), 0,
                2, TimeUnit.Week, Utils.yyyyMMddToDate(20141020));
        BudgetManager.BUDGET_MANAGER().updateBudget(b1EditDate);

        // Then 2
        info = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(info.periodIn.compareTo(BigDecimal.valueOf(4)) == 0);
        assertTrue(info.periodOut.compareTo(BigDecimal.valueOf(1)) == 0);
        assertTrue(info.periodBalance.compareTo(BigDecimal.valueOf(3)) == 0);
    }

    public void testBudgetValuesForSingleAndRecurrentTransactionsDeleteBudgetAndMoveTransaction() {
        //Given
        Utils.instrumentDateTimeNow(Utils.yyyyMMddToDate(20141112));

        Budget b1 = new Budget("b1", "b1", BigDecimal.valueOf(10), 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b1);
        Budget b2 = new Budget("b2", "b2", BigDecimal.valueOf(10), 0,
                1, TimeUnit.Month, Utils.yyyyMMddToDate(20141016));  //16-Oct -> 15-Nov
        BudgetManager.BUDGET_MANAGER().insertBudget(b2);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141014));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(1);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141116));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When
        TransactionManager.TRANSACTION_MANAGER().replaceBudget("b1", "b2");
        BudgetManager.BUDGET_MANAGER().removeBudget(b1);

        // Then
        BudgetManager.BudgetInfo infoB1 = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b1");
        assertTrue(infoB1 == null);

        BudgetManager.BudgetInfo infoB2 = BudgetManager.BUDGET_MANAGER().getBudgetInfo("b2");
        assertTrue(infoB2.periodIn.compareTo(BigDecimal.valueOf(18)) == 0);
        assertTrue(infoB2.periodOut.compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(infoB2.periodBalance.compareTo(BigDecimal.valueOf(16)) == 0);
    }

}