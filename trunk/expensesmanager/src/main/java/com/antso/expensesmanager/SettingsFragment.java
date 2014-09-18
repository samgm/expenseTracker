package com.antso.expensesmanager;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.BudgetPeriodUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionFrequencyUnit;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.utils.csv.CSVReader;

import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;


public class SettingsFragment extends Fragment {

    private DatabaseHelper dbHelper = null;
    private Map<String, Account> accountsByName;
    private Map<String, Budget> budgetsByName;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.settings_fragment, container, false);

        Button clearDataButton = (Button)layout.findViewById(R.id.clearDataButton);
        Button importMyMoneyButton = (Button)layout.findViewById(R.id.importFromMyMoneyButton);
        Button exportButton = (Button)layout.findViewById(R.id.exportButton);
        Button testButton1 = (Button)layout.findViewById(R.id.testButton1);
        Button testButton2 = (Button)layout.findViewById(R.id.testButton2);

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

            }
        });

        testButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account1 = new Account("ACC1", "Account1", BigDecimal.valueOf(1500.00), Color.rgb(0, 0, 255));
                Account account2 = new Account("ACC2", "Account2", BigDecimal.valueOf(1600.00), Color.rgb(255,0,255));
                Account account3 = new Account("ACC3", "Account3", BigDecimal.valueOf(1700.55), Color.rgb(125,125,255));
                dbHelper.insertAccount(account1);
                dbHelper.insertAccount(account2);
                dbHelper.insertAccount(account3);

                Budget budget1 = new Budget("BG1", "Budget 1", BigDecimal.valueOf(500.00), Color.BLUE, 2, BudgetPeriodUnit.Week, new DateTime(2014, 9, 1, 0, 0));
                Budget budget2 = new Budget("BG2", "Budget 2", BigDecimal.valueOf(150.00), Color.MAGENTA, 1, BudgetPeriodUnit.Month, new DateTime(2014, 9, 1, 0, 0));
                Budget budget3 = new Budget("BG3", "Budget 3", BigDecimal.valueOf(55.25), Color.YELLOW, 5, BudgetPeriodUnit.Day, new DateTime(2014, 9, 1, 0, 0));
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
                        TransactionType.Recurrent,
                        "ACC1",
                        "budget",
                        BigDecimal.valueOf(100.50),
                        new DateTime(2012, 1, 30, 0, 0));
                t.setFrequency(1);
                t.setFrequencyUnit(TransactionFrequencyUnit.Monthly);
                t.setEndDate(DateTime.now());
                dbHelper.insertTransactions(t);
            }
        });

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getActivity().getApplicationContext());
        }
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
        // ID | Type | Name | Value | Date | Budget | Account | Recurrent | Frequency | End | ID_TRANSFER |
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
            if (recurrentStr.equals("Yes")) {
                type = TransactionType.Recurrent;
            }

            return new Transaction(id, name, direction, type, account, budget, value, date);
        } catch (Exception e) {
            Log.e("TransactionParser", "Exception converting account", e);
        }

        return null;
    }
}
