package com.antso.expensesmanager.accounts;

import android.content.Context;
import android.util.Log;

import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.transactions.TransactionUpdateEvent;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;

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

public class AccountManager extends Observable {
    private static volatile AccountManager instance = null;

    private boolean started;
    private Map<String, AccountInfo> accounts;
    private DatabaseHelper dbHelper = null;

    private AccountManager() {
        accounts = new HashMap<String, AccountInfo>();
    }

    public static synchronized AccountManager ACCOUNT_MANAGER() {
        if (instance == null) {
            instance = new AccountManager();
        }

        return instance;
    }

    public void start(Context context) {
        long start = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "ACCOUNT_MANAGER(" + this + ") Start begin " + start);

        if (started) {
            return;
        }

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

        started = true;

        setChanged();
        Log.i("EXPENSES OBS", "ACCOUNT_MANAGER(" + this + ") observers: " + countObservers());
        notifyObservers(TransactionUpdateEvent.createStart());

        long end = System.currentTimeMillis();
        Log.i("EXPENSES OBS", "ACCOUNT_MANAGER(" + this + ") Start end " + end + "{" + (end - start) + "}");
    }

    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createStart());
    }

    private void createDefaultAccount() {
        Account account = new Account("DEFAULT_ACCOUNT", "Default", BigDecimal.ZERO, MaterialColours.GREY_500);
        dbHelper.insertAccount(account);
        addAccount(account);
    }

    public void stop() {
        accounts.clear();
        started = false;

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

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public void updateAccount(Account account) {
        dbHelper.updateAccount(account);
        accounts.remove(account.getId());
        addAccount(account);

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public void removeAccount(Account account) {
        accounts.remove(account.getId());
        dbHelper.deleteAccount(account.getId());

        setChanged();
        notifyObservers(TransactionUpdateEvent.createUpd(null, null));
    }

    public int size() {
        return accounts.size();
    }

    private void addAccount(Account account) {
        Collection<Transaction> transactions = TransactionManager.TRANSACTION_MANAGER()
                .getTransactionByAccount(account.getId());
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
        public Account account;

        public BigDecimal balance;
        public BigDecimal monthIn;
        public BigDecimal monthOut;
        public BigDecimal monthBalance;

        public AccountInfo(Account account, Collection<Transaction> transactions) {
            this.account = account;

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

        public void refresh(DateTime currentDate, Collection<Transaction> transactions, boolean reset, boolean remove) {
            if(reset) {
                balance = account.getInitialBalance();
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
                    if (Utils.isBeforeOrEqual(transaction.getDate(), currentDate)) {
                        balance = balance.subtract(value);
                    }
                    if (currentMonth == transactionMonth && currentYear == transactionYear) {
                        monthBalance = monthBalance.subtract(value);
                        monthOut = monthOut.add(value);
                    }
                }
                if (transaction.getDirection().equals(TransactionDirection.In)) {
                    if (Utils.isBeforeOrEqual(transaction.getDate(), currentDate)) {
                        balance = balance.add(value);
                    }

                    if (currentMonth == transactionMonth && currentYear == transactionYear) {
                        monthBalance = monthBalance.add(value);
                        monthIn = monthIn.add(value);
                    }
                }
            }

        }
    }
}
