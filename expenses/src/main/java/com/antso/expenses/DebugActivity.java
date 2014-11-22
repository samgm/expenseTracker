package com.antso.expenses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.persistence.DatabaseHelper;
import com.antso.expenses.persistence.EntityIdGenerator;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.utils.BaseAsyncTaskWithProgress;
import com.antso.expenses.utils.DataExporter;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.MyMoneyDataImporter;
import com.antso.expenses.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;


public class DebugActivity extends Activity {

    private DatabaseHelper dbHelper = null;

    public DebugActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getApplicationContext());
        }

        Button clearDataButton = (Button)findViewById(R.id.clearDataButton);
        Button importMyMoneyButton = (Button)findViewById(R.id.importFromMyMoneyButton);
        Button importButton = (Button)findViewById(R.id.importButton);
        Button exportButton = (Button)findViewById(R.id.exportButton);
        Button testButton1 = (Button)findViewById(R.id.testButton1);
        Button testButton2 = (Button)findViewById(R.id.testButton2);

        clearDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteDatabase();
            }
        });

        importMyMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BaseAsyncTaskWithProgress<Void>(DebugActivity.this, R.string.working) {
                    @Override
                    protected Void doInBackground(Void... params) {
                        MyMoneyDataImporter importer = new MyMoneyDataImporter(DebugActivity.this);
                        importer.importData();

                        BudgetManager.BUDGET_MANAGER().stop();
                        AccountManager.ACCOUNT_MANAGER().stop();
                        TransactionManager.TRANSACTION_MANAGER().stop();

                        TransactionManager.TRANSACTION_MANAGER().start(getApplicationContext());
                        AccountManager.ACCOUNT_MANAGER().start(getApplicationContext());
                        BudgetManager.BUDGET_MANAGER().start(getApplicationContext());

                        return null;
                    }
                }.execute();
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(DebugActivity.this)
                        .setTitle(R.string.title_confirm_dialog)
                        .setMessage(R.string.message_existing_all_data_will_be_deleted)
                        .setPositiveButton(R.string.sure_go_on, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new BaseAsyncTaskWithProgress<Void>(DebugActivity.this, R.string.working) {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        BudgetManager.BUDGET_MANAGER().stop();
                                        AccountManager.ACCOUNT_MANAGER().stop();
                                        TransactionManager.TRANSACTION_MANAGER().stop();

                                        dbHelper.deleteDatabase();

                                        String date = String.valueOf(Utils.dateTimeToyyyyMMdd(Utils.now()));
                                        DataExporter exporter = new DataExporter(DebugActivity.this);
                                        exporter.importData(date);

                                        TransactionManager.TRANSACTION_MANAGER().start(getApplicationContext());
                                        AccountManager.ACCOUNT_MANAGER().start(getApplicationContext());
                                        BudgetManager.BUDGET_MANAGER().start(getApplicationContext());
                                        return null;
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BaseAsyncTaskWithProgress<Void>(DebugActivity.this, R.string.working) {
                    @Override
                    protected Void doInBackground(Void... params) {
                        DataExporter exporter = new DataExporter(DebugActivity.this);
                        exporter.exportData();
                        return null;
                    }
                }.execute();
            }
        });

        testButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account1 = new Account("ACC1", "Account1", BigDecimal.valueOf(1500.00), MaterialColours.INDIGO_500);
                Account account2 = new Account("ACC2", "Account2", BigDecimal.valueOf(1600.00), MaterialColours.PINK_500);
                Account account3 = new Account("ACC3", "Account3", BigDecimal.valueOf(1700.55), MaterialColours.PURPLE_500);
                dbHelper.insertAccount(account1);
                dbHelper.insertAccount(account2);
                dbHelper.insertAccount(account3);

                Budget budget1 = new Budget("BG1", "Budget 1", BigDecimal.valueOf(500.00), MaterialColours.BLUE_500, 2, TimeUnit.Week, new DateTime(2014, 9, 1, 0, 0));
                Budget budget2 = new Budget("BG2", "Budget 2", BigDecimal.valueOf(150.00), MaterialColours.CYAN_500, 1, TimeUnit.Month, new DateTime(2014, 9, 1, 0, 0));
                Budget budget3 = new Budget("BG3", "Budget 3", BigDecimal.valueOf(55.25), MaterialColours.DEEP_ORANGE_500, 5, TimeUnit.Day, new DateTime(2014, 9, 1, 0, 0));
                dbHelper.insertBudget(budget1);
                dbHelper.insertBudget(budget2);
                dbHelper.insertBudget(budget3);
            }
        });

        testButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
                Transaction t = new Transaction(
                        id,
                        "Desc of " + id,
                        TransactionDirection.Out,
                        TransactionType.Single,
                        "ACC1",
                        "budget",
                        BigDecimal.valueOf(100.50),
                        new DateTime(2012, 1, 30, 0, 0));
                t.setRecurrent(true);
                t.setFrequency(1);
                t.setFrequencyUnit(TimeUnit.Month);
                t.setEndDate(Utils.now());
                dbHelper.insertTransactions(t);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
