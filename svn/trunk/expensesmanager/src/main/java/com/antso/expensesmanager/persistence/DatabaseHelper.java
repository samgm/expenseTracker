package com.antso.expensesmanager.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.entities.TransactionDirection;
import com.antso.expensesmanager.entities.TransactionType;
import com.antso.expensesmanager.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by asolano on 5/11/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    final private static String DB_NAME = "expenses_db";
    final private static Integer DB_VERSION = 1;
    final private Context mContext;

    //Accounts
    final static String ACCOUNT_TABLE_NAME = "Acoounts";
    final static String ACCOUNT_FIELD_ID = "Id";
    final static String ACCOUNT_FIELD_NAME = "Name";
    final static String ACCOUNT_FIELD_COLOR = "Color";
    final static String ACCOUNT_FIELD_INITIAL_BALANCE = "InitialBalance";
    final static String[] accountColumns = { ACCOUNT_FIELD_ID,
            ACCOUNT_FIELD_NAME,
            ACCOUNT_FIELD_COLOR,
            ACCOUNT_FIELD_INITIAL_BALANCE };

    final private static String ACCOUNT_CREATE_CMD =
            "CREATE TABLE " + ACCOUNT_TABLE_NAME + " ( "
                    + ACCOUNT_FIELD_ID + " TEXT NOT NULL PRIMARY KEY, "
                    + ACCOUNT_FIELD_NAME + " TEXT NOT NULL, "
                    + ACCOUNT_FIELD_COLOR + " INTEGER NOT NULL, "
                    + ACCOUNT_FIELD_INITIAL_BALANCE + " REAL );";

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

    final static String[] transactionColumns = {  TRANSACTION_FIELD_ID,
            TRANSACTION_FIELD_DESC,
            TRANSACTION_FIELD_DIRECTION,
            TRANSACTION_FIELD_TYPE,
            TRANSACTION_FIELD_ACCOUNT_ID,
            TRANSACTION_FIELD_BUDGET_ID,
            TRANSACTION_FIELD_VALUE,
            TRANSACTION_FIELD_DATE,
            TRANSACTION_FIELD_TIME };

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
                    + TRANSACTION_FIELD_TIME + " INTEGER NOT NULL );";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ACCOUNT_CREATE_CMD);
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
        db.execSQL(TRANSACTION_CREATE_CMD);
    }

    public void deleteDatabase() {
        mContext.deleteDatabase(DB_NAME);
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

        db.insert(ACCOUNT_TABLE_NAME, null, values);
    }

    public Collection<Account> getAccounts() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(ACCOUNT_TABLE_NAME,
                accountColumns, null, new String[] {}, null, null, null);

        List<Account> accounts = new ArrayList<Account>();
        while (cursor.moveToNext()) {
            Account account = new Account(cursor.getString(0), cursor.getString(1),
                    BigDecimal.valueOf(cursor.getDouble(3)), cursor.getInt(2));
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
        values.put(TRANSACTION_FIELD_DATE, Utils.dateTimeToyyyMMdd(transaction.getDateTime()));
        values.put(TRANSACTION_FIELD_TIME, Utils.dateTimeTohhMMss(transaction.getDateTime()));

        db.insert(TRANSACTION_TABLE_NAME, null, values);
    }

    public Collection<Transaction> getTransactions() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns, null, new String[] {}, null, null, null);

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            Transaction transaction = new Transaction(cursor.getString(0),
                    cursor.getString(1),
                    TransactionDirection.valueOf(cursor.getInt(2)),
                    TransactionType.valueOf(cursor.getInt(3)),
                    cursor.getString(4),
                    cursor.getString(5),
                    BigDecimal.valueOf(cursor.getDouble(6)),
                    Utils.yyyMMddhhMMssToDateTime(cursor.getInt(7), cursor.getInt(8)));

            transactions.add(transaction);
        }

        return transactions;
    }

    public Collection<Transaction> getTransactions(String accountId) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TRANSACTION_TABLE_NAME,
                transactionColumns, "AccountId = ?", new String[] { accountId }, null, null,
                TRANSACTION_FIELD_DATE + ", " + TRANSACTION_FIELD_TIME + " DESC");

        List<Transaction> transactions = new ArrayList<Transaction>();
        while (cursor.moveToNext()) {
            Transaction transaction = new Transaction(cursor.getString(0),
                    cursor.getString(1),
                    TransactionDirection.valueOf(cursor.getInt(2)),
                    TransactionType.valueOf(cursor.getInt(3)),
                    cursor.getString(4),
                    cursor.getString(5),
                    BigDecimal.valueOf(cursor.getDouble(6)),
                    Utils.yyyMMddhhMMssToDateTime(cursor.getInt(7), cursor.getInt(8)));

            transactions.add(transaction);
        }

        return transactions;
    }

    public void deleteTransaction(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TRANSACTION_TABLE_NAME,
                TRANSACTION_FIELD_ID + " = ?",
                new String[] { id });
    }

}

