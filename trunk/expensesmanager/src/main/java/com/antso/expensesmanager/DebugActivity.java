package com.antso.expensesmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.utils.csv.CSVReader;
import com.antso.expensesmanager.utils.csv.CSVWriter;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;


public class DebugActivity extends Activity {

    private DatabaseHelper dbHelper = null;
    private Map<String, Account> accountsByName;
    private Map<String, Budget> budgetsByName;

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
                importFromMyMoney();
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
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
                t.setEndDate(DateTime.now());
                dbHelper.insertTransactions(t);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void importFromMyMoney() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/MyMoney/MyMoney.csv");
//                filePath.append("/" + R.string.app_data_folder);
//                filePath.append("/" + "import.csv");

        accountsByName = AccountManager.ACCOUNT_MANAGER.getAccountsByName();
        budgetsByName = BudgetManager.BUDGET_MANAGER.getBudgetsByName();
        try {
            FileReader fileReader = new FileReader(filePath.toString());
            CSVReader reader = new CSVReader(fileReader);
            String[] values = reader.readNext();
            values = reader.readNext();
            while (values != null) {
                StringBuffer message = new StringBuffer();
                for (String val : values) {
                    message.append(val + " | ");
                }

                Log.i("CVSReader", message.toString());
                Transaction t1 = parseTransaction(values);
                Transaction t2 = null;
                if (t1.getType().equals(TransactionType.Transfer)) {
                    values = reader.readNext();
                    t2 = parseTransaction(values);
                    t1.setLinkedTransactionId(t2.getId());
                    t2.setLinkedTransactionId(t1.getId());
                }

                dbHelper.insertTransactions(t1);
                if(t2 != null) {
                    dbHelper.insertTransactions(t2);
                }

                values = reader.readNext();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Transaction parseTransaction(String values[]) {
        // ID | Type | Name | Value | Date | Budget | Account | SingleRecurrent | Frequency | End | ID_TRANSFER |
        // 1423 | Expenses | Cena Jappo Kaoru | -70.0 | 15/04/2014 | Entertainment | BNL | No |  |  |  |
        // 1386 | Revenues | Prelievo Bancomat | 60.0 | 06/03/2014 |  | Wallet | No |  |  | -2 |
        // 1387 | Expenses | Prelievo Bancomat | -60.0 | 06/03/2014 |  | BNL | No |  |  | 1386 |
        // 1038 | Expenses | Golf | -342.35 | 15/06/2013 | Car | BNL | Yes | 1 Month | 18/05/2017 |  |
        // 230 | Expenses | Divano Natuzzi | -86.45 | 15/03/2012 | Home | BNL | Yes | 1 Month | 16/01/2013 |  |
        // 65 | Expenses | Mutuo | -777.56 | 30/11/2011 | Home | BNL | Yes | 1 Month |  |  |
        try {
            String id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
            TransactionDirection direction = TransactionDirection.Undef;
            String typeStr = values[1];
            if (typeStr.equals("Expenses")) {
                direction = TransactionDirection.Out;
            } else if (typeStr.equals("Revenues")) {
                direction = TransactionDirection.In;
            }
            String name = values[2];
            BigDecimal value = BigDecimal.valueOf(Double.parseDouble(values[3]));
            value = value.abs();
            DateTime date = DateTime.parse(values[4], Utils.getDateFormatterEU());
            String budget = values[5];
            String account = values[6];
            if(accountsByName.containsKey(account)) {
                account = accountsByName.get(account).getId();
            } else {
                Log. i("TransactionParser", "Error converting account: Account not found {AccountId" + account + "}");
            }

            if(budgetsByName.containsKey(budget)) {
                budget = budgetsByName.get(budget).getId();
            } else {
                Log. i("TransactionParser", "Error converting budget: Budget not found {BudgetId" + budget + "}");
            }

            TransactionType type = TransactionType.Single;
            String recurrentStr = values[7];
            //Frequency
            //End date
            String linkedId = values[10];
            if (!linkedId.isEmpty()) {
                type = TransactionType.Transfer;
            }

            boolean recurrent = false;
            if (recurrentStr.equals("Yes")) {
                recurrent = true;
            }

            Transaction t = new Transaction(id, name, direction, type, account, budget, value, date);
            if (recurrent) {
                t.setRecurrent(true);
            }
            return t;
        } catch (Exception e) {
            Log.e("TransactionParser", "Exception converting account", e);
        }

        return null;
    }
    private void exportData() {
        exportTransactionData();
        exportAccountData();
        exportBudgetData();
    }

    private void exportTransactionData() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/" + getText(R.string.app_data_folder));
        filePath.append("transactions_export_");
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath.toString());
       } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Collection<Transaction> transactions = dbHelper.getTransactions();
        for (Transaction t : transactions) {
            String[] tString = new String[14];
            tString[0] = t.getId();
            tString[1] = t.getDescription();
            tString[2] = t.getDirection().getStringValue();
            tString[3] = t.getType().getStringValue();
            tString[4] = t.getAccountId();
            tString[5] = t.getBudgetId();
            tString[6] = t.getValue().setScale(2).toPlainString();
            tString[7] = String.valueOf(Utils.dateTimeToyyyyMMdd(t.getDate()));
            tString[8] = t.getLinkedTransactionId();
            tString[9] = String.valueOf(t.getRecurrent());
            tString[10] = String.valueOf(t.getFrequency());
            tString[11] = t.getFrequencyUnit().getStringValue();
            tString[12] = String.valueOf(Utils.dateTimeToyyyyMMdd(t.getEndDate()));
            tString[13] = String.valueOf(t.getRepetitionNum());

            writer.writeNext(tString);
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised closing file to write data", e);
            return;
        }
    }

    private void exportAccountData() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/" + getText(R.string.app_data_folder));
        filePath.append("/" + "accounts_export_");
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Collection<Account> accounts = dbHelper.getAccounts();
        for (Account a : accounts) {
            String[] aString = new String[4];

            aString[0] = a.getId();
            aString[1] = a.getName();
            aString[2] = a.getInitialBalance().setScale(2).toPlainString();
            aString[3] = String.valueOf(a.getColor());

            writer.writeNext(aString);
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised closing file to write data", e);
            return;
        }
    }

    private void exportBudgetData() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/" + getText(R.string.app_data_folder));
        filePath.append("/" + "budgets_export_");
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Collection<Budget> budgets = dbHelper.getBudgets();
        for (Budget b : budgets) {
            String[] bString = new String[7];

            bString[0] = b.getId();
            bString[1] = b.getName();
            bString[2] = b.getThreshold().setScale(2).toPlainString();
            bString[3] = String.valueOf(b.getColor());
            bString[4] = b.getPeriodUnit().getStringValue();
            bString[5] = String.valueOf(b.getPeriodLength());
            bString[6] = String.valueOf(Utils.dateTimeToyyyyMMdd(b.getPeriodStart()));

            writer.writeNext(bString);
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised closing file to write data", e);
            return;
        }
    }

    private void importData() {
        StringBuffer filePath = new StringBuffer();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/MyMoney/MyMoney.csv");
//                filePath.append("/" + R.string.app_data_folder);
//                filePath.append("/" + "import.csv");

        accountsByName = AccountManager.ACCOUNT_MANAGER.getAccountsByName();
        budgetsByName = BudgetManager.BUDGET_MANAGER.getBudgetsByName();
        try {
            FileReader fileReader = new FileReader(filePath.toString());
            CSVReader reader = new CSVReader(fileReader);
            String[] values = reader.readNext();
            values = reader.readNext();
            while (values != null) {
                StringBuffer message = new StringBuffer();
                for (String val : values) {
                    message.append(val + " | ");
                }

                Log.i("CVSReader", message.toString());
                Transaction t1 = parseTransaction(values);
                Transaction t2 = null;
                if (t1.getType().equals(TransactionType.Transfer)) {
                    values = reader.readNext();
                    t2 = parseTransaction(values);
                    t1.setLinkedTransactionId(t2.getId());
                    t2.setLinkedTransactionId(t1.getId());
                }

                dbHelper.insertTransactions(t1);
                if(t2 != null) {
                    dbHelper.insertTransactions(t2);
                }

                values = reader.readNext();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
