package com.antso.expensesmanager.accounts;

import android.content.Context;
import android.util.Log;

import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.MaterialColours;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AccountManager {
        ACCOUNT_MANAGER;

    private Map<String, AccountInfo> accounts;
    private DatabaseHelper dbHelper = null;

    private AccountManager() {
        accounts = new HashMap<String, AccountInfo>();
    }

    public void start(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);

            Collection<Account> accounts = dbHelper.getAccounts();
            for (Account account : accounts) {
                addAccount(account);
            }

            if (accounts.size() == 0) {
                createDefaultAccount();
            }
        }
    }

    private void createDefaultAccount() {
        Account account = new Account("DEFAULT_ACCOUNT", "Default", BigDecimal.ZERO, MaterialColours.GREY_500);
        dbHelper.insertAccount(account);
        addAccount(account);
    }

    public void stop() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void onTransactionAdded(Transaction transaction) {
        AccountInfo accountInfo = accounts.get(transaction.getAccountId());
        if (accountInfo != null) {
            accountInfo.addTransaction(transaction);
        }
    }

    public void onTransactionDeleted(Transaction transaction) {
        AccountInfo accountInfo = accounts.get(transaction.getAccountId());
        if (accountInfo != null) {
            accountInfo.removeTransaction(transaction);
        }
    }

    public void insertAccount(Account account) {
        dbHelper.insertAccount(account);
        addAccount(account);
    }

    public void removeAccount(Account account) {
        accounts.remove(account.getId());
        dbHelper.deleteAccount(account.getId());
    }

    public int size() {
        return accounts.size();
    }

    private void addAccount(Account account) {
        Collection<Transaction> transactions = dbHelper.getTransactionsByAccount(account.getId());
        AccountInfo accountInfo = new AccountInfo(account, transactions);
        accounts.put(account.getId(), accountInfo);
    }

    public List<AccountInfo> getAccountInfo() {
        List<AccountInfo> accountInfoList = new ArrayList<AccountInfo>(accounts.size());
        for(AccountInfo info : accounts.values()) {
            accountInfoList.add(info);
        }
        return accountInfoList;
    }

    public AccountInfo getAccountInfo(String accountId) {
        return accounts.get(accountId);
    }

    public Map<String, Account> getAccountsByName() {
        Map<String, Account> accountByName = new HashMap<String, Account>(accounts.size());
        for(AccountInfo info : accounts.values()) {
            if(!accountByName.containsKey(info.account.getName())) {
                accountByName.put(info.account.getName(), info.account);
            } else {
                Log. i("AccountManager", "Error creating accountByName map: Account with this name already added {Name " + info.account.getName() + "}");
            }
        }
        return accountByName;
    }

    public Collection<Account> getAccounts() {
        return dbHelper.getAccounts();
    }


    public class AccountInfo {
        public Collection<Transaction> transactions = new ArrayList<Transaction>();
        public Account account;

        public BigDecimal balance;
        public BigDecimal monthIn;
        public BigDecimal monthOut;
        public BigDecimal monthBalance;
        public Map<String, BigDecimal> byMonthBalances;

        public AccountInfo(Account account, Collection<Transaction> transactions) {
            this.account = account;
            this.byMonthBalances = new HashMap<String, BigDecimal>();

            for (Transaction transaction : transactions) {
                this.transactions.add(transaction);
                if (transaction.getRecurrent() && !transaction.isAutoGenerated()) {
                    this.transactions.addAll(
                            TransactionManager.explodeRecurrentTransaction(transaction, DateTime.now()));
                }
            }
            refresh(DateTime.now(), this.transactions, true, false);
        }

        public void addTransaction(Transaction transaction) {
            transactions.add(transaction);
            refresh(DateTime.now(), Collections.singleton(transaction), false, false);
        }

        public void removeTransaction(Transaction transaction) {
            transactions.remove(transaction);
            refresh(DateTime.now(), Collections.singleton(transaction), false, true);
        }

        public void refresh(DateTime currentDate, Collection<Transaction> transactions, boolean reset, boolean remove) {
            if(reset) {
                balance = BigDecimal.ZERO;
                monthOut = BigDecimal.ZERO;
                monthIn = BigDecimal.ZERO;
                monthBalance = BigDecimal.ZERO;
            }

            for (Transaction transaction : transactions) {
                int currentMonth = currentDate.getMonthOfYear();
                int currentYear = currentDate.getYear();
                int transactionMonth = transaction.getDate().getMonthOfYear();
                int transactionYear = transaction.getDate().getYear();

                BigDecimal value = transaction.getValue();
                if (remove) {
                    value = value.negate();
                }

                if (transaction.getDirection().equals(TransactionDirection.Out)) {
                    balance = balance.subtract(value);

                    if (currentMonth == transactionMonth && currentYear == transactionYear) {
                        monthBalance = monthBalance.subtract(value);
                        monthOut = monthOut.add(value);
                    }
                }
                if (transaction.getDirection().equals(TransactionDirection.In)) {
                    balance = balance.add(value);

                    if (currentMonth == transactionMonth && currentYear == transactionYear) {
                        monthBalance = monthBalance.add(value);
                        monthIn = monthIn.add(value);
                    }
                }

                //TODO manage update on byMonthBalances when refresh with reset == false
                String key = transactionYear + "_" + transactionMonth;
                byMonthBalances.put(key, balance);
            }

        }

        public BigDecimal getByDateBalance(DateTime dateTime) {
            String key = dateTime.getYear() + "_" + dateTime.getMonthOfYear();
            BigDecimal result = byMonthBalances.get(key);

            return (result != null) ? result : BigDecimal.ZERO;
        }
    }
}
