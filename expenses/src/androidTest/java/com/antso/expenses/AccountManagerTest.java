package com.antso.expenses;

import android.test.AndroidTestCase;

import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.persistence.DatabaseHelper;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.Utils;

import java.math.BigDecimal;

public class AccountManagerTest extends AndroidTestCase {

    Transaction goodIn;
    Transaction wrongAccount;
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
        wrongAccount = new Transaction("t2", "t2", TransactionDirection.Out, TransactionType.Single, "a2", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141010));
        goodIn = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(8), Utils.yyyyMMddToDate(20141016));
        goodOut =  new Transaction("t3", "t3", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(3), Utils.yyyyMMddToDate(20141014));
        wrongDate1 =  new Transaction("t4", "t4", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141116));
        wrongDate2 =  new Transaction("t5", "t5", TransactionDirection.Out, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(3), Utils.yyyyMMddToDate(20140930));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongAccount);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate2);
        TransactionManager.TRANSACTION_MANAGER().insertTransaction(wrongDate1);
    }

    private void delTransactionsForTest() {
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodIn);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongAccount);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodOut);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate1);
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(wrongDate2);
    }

    public void testAccountValuesForSingleTransactionsWhenAddAndDel() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        // When 1
        createAndAddTransactionsForTest();

        // Then 1
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(8)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(5)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(12)) == 0);

        // When 2
        AccountManager.ACCOUNT_MANAGER().stop();
        AccountManager.ACCOUNT_MANAGER().start(getContext());

        // Then 2
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(8)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(5)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(12)) == 0);

        // When 3
        delTransactionsForTest();

        // Then 3
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(10)) == 0);
    }

    public void testAccountValuesForSingleTransactionsWhenEdit() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForTest();

        // When 1
        Transaction goodChangedValue = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedValue);

        // Then 1
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(5)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When 2
        Transaction goodChangedAccount = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a2", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141016));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedAccount);

        // Then 2
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(-3)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(4)) == 0);

        // When 3
        Transaction goodChangedDate1 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20141005));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate1);

        // Then 3
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(5)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When 4
        Transaction goodChangedDate2 = new Transaction("t1", "t1", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(5), Utils.yyyyMMddToDate(20140929));
        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodChangedDate2);

        // Then 4
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(-3)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(9)) == 0);

        // When 5
        AccountManager.ACCOUNT_MANAGER().stop();
        AccountManager.ACCOUNT_MANAGER().start(getContext());

        // Then 5
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(-3)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(9)) == 0);
    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenAddAndDel() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        // When 1
        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(1), Utils.yyyyMMddToDate(20141002));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(2);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // Then 1
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(14)) == 0);

        // When 2
        AccountManager.ACCOUNT_MANAGER().stop();
        AccountManager.ACCOUNT_MANAGER().start(getContext());

        // Then 2
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(14)) == 0);

        // When 3
        delTransactionsForTest();
        TransactionManager.TRANSACTION_MANAGER().removeTransaction(goodRecurrent);

        // Then 3
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(0)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(10)) == 0);
    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenEdit() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        // When 1
        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(1), Utils.yyyyMMddToDate(20141002));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(2);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // Then 1
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(14)) == 0);

        // When 2
        Transaction goodRecurrentEdit1 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141002));
        goodRecurrentEdit1.setRecurrent(true);
        goodRecurrentEdit1.setFrequency(2);
        goodRecurrentEdit1.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit1.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit1);

        // Then 2
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");

        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(12)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(9)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(16)) == 0);

        // When 3
        Transaction goodRecurrentEdit2 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141010));
        goodRecurrentEdit2.setRecurrent(true);
        goodRecurrentEdit2.setFrequency(2);
        goodRecurrentEdit2.setFrequencyUnit(TimeUnit.Week);
        goodRecurrentEdit2.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit2);

        // Then 3
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(14)) == 0);

        // When 4
        Transaction goodRecurrentEdit3 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141010));
        goodRecurrentEdit3.setRecurrent(true);
        goodRecurrentEdit3.setFrequency(1);
        goodRecurrentEdit3.setFrequencyUnit(TimeUnit.Day);
        goodRecurrentEdit3.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit3);

        // Then 4
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(22)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(19)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(26)) == 0);

        // When 5
        Transaction goodRecurrentEdit4 = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(2), Utils.yyyyMMddToDate(20141010));
        goodRecurrentEdit4.setRecurrent(false);
        goodRecurrentEdit4.setFrequency(1);
        goodRecurrentEdit4.setFrequencyUnit(TimeUnit.Day);
        goodRecurrentEdit4.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().updateTransaction(goodRecurrentEdit4);

        // Then 5
        info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(14)) == 0);
    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenEditAccountValue() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(1), Utils.yyyyMMddToDate(20141002));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(2);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When
        Account a1Edit = new Account("a1", "a1", BigDecimal.valueOf(15), 0, false);
        AccountManager.ACCOUNT_MANAGER().updateAccount(a1Edit);

        // Then
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(info.monthOut.compareTo(BigDecimal.valueOf(3)) == 0);
        assertTrue(info.monthBalance.compareTo(BigDecimal.valueOf(7)) == 0);
        assertTrue(info.balance.compareTo(BigDecimal.valueOf(19)) == 0);
    }

    public void testAccountValuesForSingleAndRecurrentTransactionsWhenDeleteAccountAndTransactions() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(1), Utils.yyyyMMddToDate(20141002));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(2);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When
        AccountManager.ACCOUNT_MANAGER().removeAccount(a1);

        // Then
        AccountManager.AccountInfo info = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(info == null);
    }

    public void testAccountValuesForSingleAndRecurrentTransactionsDeleteAccountAndMoveTransaction() {
        //Given
        Account a1 = new Account("a1", "a1", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a1);
        Account a2 = new Account("a2", "a2", BigDecimal.valueOf(10), 0, false);
        AccountManager.ACCOUNT_MANAGER().insertAccount(a2);

        createAndAddTransactionsForTest();
        Transaction goodRecurrent = new Transaction("t6", "t6", TransactionDirection.In, TransactionType.Single, "a1", "b1", BigDecimal.valueOf(1), Utils.yyyyMMddToDate(20141002));
        goodRecurrent.setRecurrent(true);
        goodRecurrent.setFrequency(2);
        goodRecurrent.setFrequencyUnit(TimeUnit.Week);
        goodRecurrent.setEndDate(Utils.yyyyMMddToDate(20141108));

        TransactionManager.TRANSACTION_MANAGER().insertTransaction(goodRecurrent);

        // When
        TransactionManager.TRANSACTION_MANAGER().replaceAccount("a1", "a2");
        AccountManager.ACCOUNT_MANAGER().removeAccount(a1);

        // Then
        AccountManager.AccountInfo infoA1 = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a1");
        assertTrue(infoA1 == null);

        AccountManager.AccountInfo infoA2 = AccountManager.ACCOUNT_MANAGER().getAccountInfo("a2");
        assertTrue(infoA2.monthIn.compareTo(BigDecimal.valueOf(10)) == 0);
        assertTrue(infoA2.monthOut.compareTo(BigDecimal.valueOf(8)) == 0);
        assertTrue(infoA2.monthBalance.compareTo(BigDecimal.valueOf(2)) == 0);
        assertTrue(infoA2.balance.compareTo(BigDecimal.valueOf(9)) == 0);
    }

}