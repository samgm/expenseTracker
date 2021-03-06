package com.antso.expenses.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    final private static String DB_NAME = "expenses_db";
    final private static Integer DB_VERSION = 2;
    final private Context mContext;

    //Accounts
    final static String ACCOUNT_TABLE_NAME = "Acoounts";
    final static String ACCOUNT_FIELD_ID = "Id";
    final static String ACCOUNT_FIELD_NAME = "Name";
    final static String ACCOUNT_FIELD_COLOR = "Color";
    final static String ACCOUNT_FIELD_INITIAL_BALANCE = "InitialBalance";
    final static String ACCOUNT_FIELD_ARCHIVED = "Archived";
    final static String[] accountColumns = { ACCOUNT_FIELD_ID,
            ACCOUNT_FIELD_NAME,
            ACCOUNT_FIELD_COLOR,
            ACCOUNT_FIELD_INITIAL_BALANCE,
            ACCOUNT_FIELD_ARCHIVED};

    final private static String ACCOUNT_CREATE_CMD =
            "CREATE TABLE " + ACCOUNT_TABLE_NAME + " ( "
                    + ACCOUNT_FIELD_ID + " TEXT NOT NULL PRIMARY KEY, "
                    + ACCOUNT_FIELD_NAME + " TEXT NOT NULL, "
                    + ACCOUNT_FIELD_COLOR + " INTEGER NOT NULL, "
                    + ACCOUNT_FIELD_INITIAL_BALANCE + " REAL, "
                    + ACCOUNT_FIELD_ARCHIVED + " INTEGER NOT NULL );";

    final private static String ACCOUNT_DELETE_CMD = "DROP TABLE " + ACCOUNT_TABLE_NAME;

    //Budgets
    final static String BUDGET_TABLE_NAME = "Budgets";
    final static String BUDGET_FIELD_ID = "Id";
    final static String BUDGET_FIELD_NAME = "Name";
    final static String BUDGET_FIELD_THRESHOLD = "Threshold";
    final static String BUDGET_FIELD_COLOR = "Color";
    final static String BUDGET_FIELD_PERIOD_UNIT = "PeriodUnit";
    final static String BUDGET_FIELD_PERIOD_LENGTH = "PeriodLength";
    final static String BUDGET_FIELD_PERIOD_START = "PeriodStart";

    final static String[] budgetColumns = { BUDGET_FIELD_ID,
            BUDGET_FIELD_NAME,
            BUDGET_FIELD_THRESHOLD,
            BUDGET_FIELD_COLOR,
            BUDGET_FIELD_PERIOD_UNIT,
            BUDGET_FIELD_PERIOD_LENGTH,
            BUDGET_FIELD_PERIOD_START };

    final private static String BUDGET_CREATE_CMD =
            "CREATE TABLE " + BUDGET_TABLE_NAME + " ( "
                    + BUDGET_FIELD_ID + " TEXT NOT NULL PRIMARY KEY, "
                    + BUDGET_FIELD_NAME + " TEXT NOT NULL, "
                    + BUDGET_FIELD_THRESHOLD + " REAL, "
                    + BUDGET_FIELD_COLOR + " INTEGER NOT NULL, "
                    + BUDGET_FIELD_PERIOD_UNIT + " INTEGER NOT NULL, "
                    + BUDGET_FIELD_PERIOD_LENGTH + " INTEGER NOT NULL,"
                    + BUDGET_FIELD_PERIOD_START + " INTEGER NOT NULL );";

    final private static String BUDGET_DELETE_CMD = "DROP TABLE " + BUDGET_TABLE_NAME;

    //Transactions
    final static String TRANSACTION_TABLE_NAME = "Transactions";
    final static String TRANSACTION_FIELD_ID = "Id";
    final static String TRANSACTION_FIELD_DESC = "Description";
    final static String TRANSACTION_FIELD_DIRECTION = "Direction";
    final static String TRANSACTION_FIELD_TYPE = "Type";
    final static String TRANSACTION_FIELD_ACCOUNT_ID = "AccountId";
    final static String TRANSACTION_FIELD_BUDGET_ID = "BudgetId";
    final static String TRANSACTION_FIELD_VALUE = "Value";
    final static String TRANSACTION_FIELD_DATE = "Date";
    final static String TRANSACTION_FIELD_TIME = "Time";
    final static String TRANSACTION_FIELD_LINKED_TRANSACTION_ID = "LinkedTransactionId";
    final static String TRANSACTION_FIELD_RECURRENT = "Recurrent";
    final static String TRANSACTION_FIELD_FREQUENCY = "Frequency";
    final static String TRANSACTION_FIELD_FREQUENCY_UNIT = "FrequencyUnit";
    final static String TRANSACTION_FIELD_END = "End";
    final static String TRANSACTION_FIELD_REPETITION_NUM = "RepetitionNum";
    final static String TRANSACTION_FIELD_HAS_FEE = "HasFee";
    final static String TRANSACTION_FIELD_FEE_TRANSACTION_ID= "FeeTransactionId";


    final static String[] transactionColumns = {  TRANSACTION_FIELD_ID,
            TRANSACTION_FIELD_DESC,
            TRANSACTION_FIELD_DIRECTION,
            TRANSACTION_FIELD_TYPE,
            TRANSACTION_FIELD_ACCOUNT_ID,
            TRANSACTION_FIELD_BUDGET_ID,
            TRANSACTION_FIELD_VALUE,
            TRANSACTION_FIELD_DATE,
            TRANSACTION_FIELD_TIME,
            TRANSACTION_FIELD_LINKED_TRANSACTION_ID,
            TRANSACTION_FIELD_RECURRENT,
            TRANSACTION_FIELD_FREQUENCY,
            TRANSACTION_FIELD_FREQUENCY_UNIT,
            TRANSACTION_FIELD_END,
            TRANSACTION_FIELD_REPETITION_NUM,
            TRANSACTION_FIELD_HAS_FEE,
            TRANSACTION_FIELD_FEE_TRANSACTION_ID};

    final private static String TRANSACTION_CREATE_CMD =
            "CREATE TABLE " + TRANSACTION_TABLE_NAME + " ( "
                    + TRANSACTION_FIELD_ID + " TEXT NOT NULL PRIMARY KEY, "
                    + TRANSACTION_FIELD_DESC + " TEXT , "
                    + TRANSACTION_FIELD_DIRECTION + " INTEGER, "
                    + TRANSACTION_FIELD_TYPE + " INTEGER, "
                    + TRANSACTION_FIELD_ACCOUNT_ID + " TEXT NOT NULL, "
                    + TRANSACTION_FIELD_BUDGET_ID + " TEXT NOT NULL, "
                    + TRANSACTION_FIELD_VALUE + " REAL, "
                    + TRANSACTION_FIELD_DATE + " INTEGER NOT NULL, "
                    + TRANSACTION_FIELD_TIME + " INTEGER NOT NULL, "
                    + TRANSACTION_FIELD_LINKED_TRANSACTION_ID + " TEXT, "
                    + TRANSACTION_FIELD_RECURRENT + " INTEGER NOT NULL, "
                    + TRANSACTION_FIELD_FREQUENCY + " INTEGER, "
                    + TRANSACTION_FIELD_FREQUENCY_UNIT + " INTEGER, "
                    + TRANSACTION_FIELD_END + " INTEGER, "
                    + TRANSACTION_FIELD_REPETITION_NUM + " INTEGER, "
                    + TRANSACTION_FIELD_HAS_FEE + " INTEGER NOT NULL, "
                    + TRANSACTION_FIELD_FEE_TRANSACTION_ID + " TEXT );";

    final private static String TRANSACTION_DELETE_CMD = "DROP TABLE " + TRANSACTION_TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;

        SQLiteDatabase db = getReadableDatabase();
        long accounts = DatabaseUtils.queryNumEntries(db, ACCOUNT_TABLE_NAME);
        long budgets = DatabaseUtils.queryNumEntries(db, BUDGET_TABLE_NAME);
        long transactions = DatabaseUtils.queryNumEntries(db, TRANSACTION_TABLE_NAME);

        EntityIdGenerator.ENTITY_ID_GENERATOR.registerEntity(Account.class, "A", accounts, true);
        EntityIdGenerator.ENTITY_ID_GENERATOR.registerEntity(Budget.class, "B", budgets, true);
        EntityIdGenerator.ENTITY_ID_GENERATOR.registerEntity(Transaction.class, "T", transactions, true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ACCOUNT_CREATE_CMD);
            db.execSQL(BUDGET_CREATE_CMD);
            db.execSQL(TRANSACTION_CREATE_CMD);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Exception raised: " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DatabaseHelper", "On upgrade not supported");
        mContext.deleteDatabase(DB_NAME);

        db.execSQL(ACCOUNT_CREATE_CMD);
        db.execSQL(BUDGET_CREATE_CMD);
        db.execSQL(TRANSACTION_CREATE_CMD);
    }

    public void deleteDatabase() {
        mContext.deleteDatabase(DB_NAME);
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(ACCOUNT_DELETE_CMD);
        db.execSQL(BUDGET_DELETE_CMD);
        db.execSQL(TRANSACTION_DELETE_CMD);

        db.execSQL(ACCOUNT_CREATE_CMD);
        db.execSQL(BUDGET_CREATE_CMD);
        db.execSQL(TRANSACTION_CREATE_CMD);
    }

    // ACCOUNTS
    //--------------------------

    public void insertAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ACCOUNT_FIELD_ID, account.getId());
        values.put(ACCOUNT_FIELD_NAME, account.getName());
        values.put(ACCOUNT_FIELD_COLOR, account.getColor());
        values.put(ACCOUNT_FIELD_INITIAL_BALANCE, account.getInitialBalance().doubleValue());
        values.put(ACCOUNT_FIELD_ARCHIVED, account.isArchived());

        db.insert(ACCOUNT_TABLE_NAME, null, values);
    }

    public void updateAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ACCOUNT_FIELD_NAME, account.getName());
        values.put(ACCOUNT_FIELD_COLOR, account.getColor());
        values.put(ACCOUNT_FIELD_INITIAL_BALANCE, account.getInitialBalance().doubleValue());
        values.put(ACCOUNT_FIELD_ARCHIVED, account.isArchived());

        db.update(ACCOUNT_TABLE_NAME, values,
                ACCOUNT_FIELD_ID + " = ?", new String[]{account.getId()});
    }

    public List<Account> getAccounts() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(ACCOUNT_TABLE_NAME,
                accountColumns, null, new String[] {}, null, null, null);

        List<Account> accounts = new ArrayList<Account>();
        while (cursor.moveToNext()) {
            Account account = new Account(cursor.getString(0), cursor.getString(1),
                    BigDecimal.valueOf(cursor.getDouble(3)), cursor.getInt(2),
                    (cursor.getInt(4) == 1) ? true : false);
            accounts.add(account);
        }

        return accounts;
    }

    public void deleteAccount(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ACCOUNT_TABLE_NAME,
                ACCOUNT_FIELD_ID + " = ?",
                new String[] { id });
    }

    // BUDGETS
    //--------------------------

    public void insertBudget(Budget budget) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BUDGET_FIELD_ID, budget.getId());
        values.put(BUDGET_FIELD_NAME, budget.getName());
        values.put(BUDGET_FIELD_THRESHOLD, budget.getThreshold().doubleValue());
        values.put(BUDGET_FIELD_COLOR, budget.getColor());
        values.put(BUDGET_FIELD_PERIOD_UNIT, budget.getPeriodUnit().getIntValue());
        values.put(BUDGET_FIELD_PERIOD_LENGTH, budget.getPeriodLength());
        values.put(BUDGET_FIELD_PERIOD_START, Utils.dateTimeToyyyyMMdd(budget.getPeriodStart()));

        db.insert(BUDGET_TABLE_NAME, null, values);
    }

    public void updateBudget(Budget budget) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BUDGET_FIELD_NAME, budget.getName());
        values.put(BUDGET_FIELD_THRESHOLD, budget.getThreshold().doubleValue());
        values.put(BUDGET_FIELD_COLOR, budget.getColor());
        values.put(BUDGET_FIELD_PERIOD_UNIT, budget.getPeriodUnit().getIntValue());
        values.put(BUDGET_FIELD_PERIOD_LENGTH, budget.getPeriodLength());
        values.put(BUDGET_FIELD_PERIOD_START, Utils.dateTimeToyyyyMMdd(budget.getPeriodStart()));

        db.update(BUDGET_TABLE_NAME, values,
                BUDGET_FIELD_ID + " = ?", new String[] { budget.getId() });
    }

    public List<Budget> getBudgets() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(BUDGET_TABLE_NAME,
                budgetColumns, null, new String[] {}, null, null, null);

        List<Budget> budgets = new ArrayList<Budget>();
        while (cursor.moveToNext()) {
            Budget budget = new Budget(cursor.getString(0), cursor.getString(1),
                    BigDecimal.valueOf(cursor.getDouble(2)), cursor.getInt(3),
                    cursor.getInt(5),
                    TimeUnit.valueOf(cursor.getInt(4)),
                    Utils.yyyyMMddToDate(cursor.getInt(6)));
            budgets.add(budget);
        }

        return budgets;
    }

    public void deleteBudget(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(BUDGET_TABLE_NAME,
                BUDGET_FIELD_ID + " = ?",
                new String[] { id });
    }

    // TRANSACTIONS
    //--------------------------

    public void insertTransactions(Transaction transaction) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TRANSACTION_FIELD_ID, transaction.getId());
        values.put(TRANSACTION_FIELD_DESC, transaction.getDescription());
        values.put(TRANSACTION_FIELD_DIRECTION, transaction.getDirection().getIntValue());
        values.put(TRANSACTION_FIELD_TYPE, transaction.getType().getIntValue());
        values.put(TRANSACTION_FIELD_ACCOUNT_ID, transaction.getAccountId());
        values.put(TRANSACTION_FIELD_BUDGET_ID, transaction.getBudgetId());
        values.put(TRANSACTION_FIELD_VALUE, transaction.getValue().doubleValue());
        values.put(TRANSACTION_FIELD_DATE, Utils.dateTimeToyyyyMMdd(transaction.getDate()));
        values.put(TRANSACTION_FIELD_TIME, Utils.dateTimeTohhMMss(transaction.getDate()));
        values.put(TRANSACTION_FIELD_LINKED_TRANSACTION_ID, transaction.getLinkedTransactionId());
        values.put(TRANSACTION_FIELD_RECURRENT, transaction.getRecurrent());
        values.put(TRANSACTION_FIELD_FREQUENCY, transaction.getFrequency());
        values.put(TRANSACTION_FIELD_FREQUENCY_UNIT, transaction.getFrequencyUnit().getIntValue());
        values.put(TRANSACTION_FIELD_END, Utils.dateTimeToyyyyMMdd(transaction.getEndDate()));
        values.put(TRANSACTION_FIELD_REPETITION_NUM, transaction.getRepetitionNum());
        values.put(TRANSACTION_FIELD_HAS_FEE, transaction.hasFee());
        values.put(TRANSACTION_FIELD_FEE_TRANSACTION_ID, transaction.getFeeTransactionId());

        db.insert(TRANSACTION_TABLE_NAME, null, values);
    }
    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction(cursor.getString(0),
                cursor.getString(1),
                TransactionDirection.valueOf(cursor.getInt(2)),
                TransactionType.valueOf(cursor.getInt(3)),
                cursor.getString(4),
                cursor.getString(5),
                BigDecimal.valueOf(cursor.getDouble(6)),
                Utils.yyyyMMddhhMMssToDateTime(cursor.getInt(7), cursor.getInt(8)));

        transaction.setLinkedTransactionId(cursor.getString(9));
        transaction.setRecurrent(cursor.getInt(10) == 1);
        transaction.setFrequency(cursor.getInt(11));
        transaction.setFrequencyUnit(TimeUnit.valueOf(cursor.getInt(12)));
        transaction.setEndDate(Utils.yyyyMMddToDate(cursor.getInt(13)));
        transaction.setRepetitionNum(cursor.getInt(14));
        transaction.setFeeTransactionId(cursor.getString(16));

        return transaction;
    }

    public List<Transaction> getTransactions() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns, null, new String[] {}, null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }

        return transactions;
    }

//    public Transaction getTransactionsById(String id) {
//        SQLiteDatabase db = getWritableDatabase();
//
//        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
//                transactionColumns,
//                TRANSACTION_FIELD_ID + " = ?", new String[] { id },
//                null, null, null);
//
//        if(cursor.moveToNext()) {
//            return cursorToTransaction(cursor);
//        }
//
//        return null;
//    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns,
                TRANSACTION_FIELD_ACCOUNT_ID + " = ?", new String[] { accountId },
                null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByAccountAndDate(String accountId, DateTime date) {
        SQLiteDatabase db = getWritableDatabase();
        String dateStr = String.valueOf(Utils.dateTimeToyyyyMMdd(date) / 100);
        dateStr = dateStr + "%";

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns,
                TRANSACTION_FIELD_ACCOUNT_ID + " = ? AND " +
                TRANSACTION_FIELD_DATE + " LIKE ?", new String[] { accountId, dateStr },
                null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByBudget(String budgetId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns,
                TRANSACTION_FIELD_BUDGET_ID + " = ?", new String[] { budgetId },
                null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByBudgetAndDate(String budgetId, DateTime start, DateTime end) {
        SQLiteDatabase db = getWritableDatabase();
        String startStr = String.valueOf(Utils.dateTimeToyyyyMMdd(start));
        String endStr = String.valueOf(Utils.dateTimeToyyyyMMdd(end));

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns,
                TRANSACTION_FIELD_BUDGET_ID + " = ? AND " +
                TRANSACTION_FIELD_DATE + " >= ? AND " +
                TRANSACTION_FIELD_DATE + " < ?",
                new String[] { budgetId, startStr, endStr },
                null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }

        return transactions;
    }

//    public List<Transaction> getTransactions(TransactionDirection direction, boolean noTransfer) {
//        SQLiteDatabase db = getWritableDatabase();
//
//        Integer directionInt = direction.getIntValue();
//        String directionStr = directionInt.toString();
//        Integer typeInt = TransactionType.Transfer.getIntValue();
//        String typeStr = typeInt.toString();
//        Cursor cursor;
//        if (noTransfer) {
//            cursor = db.query(TRANSACTION_TABLE_NAME,
//                    transactionColumns,
//                    TRANSACTION_FIELD_DIRECTION + " = ? AND " + TRANSACTION_FIELD_TYPE + " != ?",
//                    new String[]{directionStr, typeStr},
//                    null, null,
//                    TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");
//        } else {
//            cursor = db.query(TRANSACTION_TABLE_NAME,
//                    transactionColumns,
//                    TRANSACTION_FIELD_DIRECTION + " = ?", new String[]{directionStr},
//                    null, null,
//                    TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");
//        }
//        List<Transaction> transactions = new ArrayList<Transaction>();
//        while (cursor.moveToNext()) {
//            transactions.add(cursorToTransaction(cursor));
//        }
//
//        return transactions;
//    }

//    public List<Transaction> getTransactions(TransactionType type) {
//        SQLiteDatabase db = getWritableDatabase();
//
//        Integer typeInt = type.getIntValue();
//        String typeStr = typeInt.toString();
//        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
//                transactionColumns,
//                TRANSACTION_FIELD_TYPE + " = ?", new String[] { typeStr },
//                null, null,
//                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");
//
//        List<Transaction> transactions = new ArrayList<Transaction>();
//        while (cursor.moveToNext()) {
//            transactions.add(cursorToTransaction(cursor));
//        }
//
//        return transactions;
//    }

    public void deleteTransaction(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TRANSACTION_TABLE_NAME,
                TRANSACTION_FIELD_ID + " = ?",
                new String[] { id });
    }

    public void updateTransaction(Transaction transaction) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_FIELD_DESC, transaction.getDescription());
        values.put(TRANSACTION_FIELD_DIRECTION, transaction.getDirection().getIntValue());
        values.put(TRANSACTION_FIELD_TYPE, transaction.getType().getIntValue());
        values.put(TRANSACTION_FIELD_ACCOUNT_ID, transaction.getAccountId());
        values.put(TRANSACTION_FIELD_BUDGET_ID, transaction.getBudgetId());
        values.put(TRANSACTION_FIELD_VALUE, transaction.getValue().doubleValue());
        values.put(TRANSACTION_FIELD_DATE, Utils.dateTimeToyyyyMMdd(transaction.getDate()));
        values.put(TRANSACTION_FIELD_TIME, Utils.dateTimeTohhMMss(transaction.getDate()));
        values.put(TRANSACTION_FIELD_LINKED_TRANSACTION_ID, transaction.getLinkedTransactionId());
        values.put(TRANSACTION_FIELD_RECURRENT, transaction.getRecurrent());
        values.put(TRANSACTION_FIELD_FREQUENCY, transaction.getFrequency());
        values.put(TRANSACTION_FIELD_FREQUENCY_UNIT, transaction.getFrequencyUnit().getIntValue());
        values.put(TRANSACTION_FIELD_END, Utils.dateTimeToyyyyMMdd(transaction.getEndDate()));
        values.put(TRANSACTION_FIELD_REPETITION_NUM, transaction.getRepetitionNum());
        values.put(TRANSACTION_FIELD_HAS_FEE, transaction.hasFee());
        values.put(TRANSACTION_FIELD_FEE_TRANSACTION_ID, transaction.getFeeTransactionId());

        db.update(TRANSACTION_TABLE_NAME, values,
                TRANSACTION_FIELD_ID + " = ?",
                new String[] { transaction.getId() });
    }

}

