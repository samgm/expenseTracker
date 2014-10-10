package com.antso.expensesmanager.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

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
import com.antso.expensesmanager.utils.csv.CSVReader;

import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class MyMoneyDataImporter {
    private final Context context;

    private Map<String, Account> accountsByName;
    private Map<String, Budget> budgetsByName;

    public MyMoneyDataImporter(final Context context) {
        this.context = context;
    }

    public void importData() {
        StringBuilder filePath = new StringBuilder();
        filePath.append(Environment.getExternalStorageDirectory());
        filePath.append("/MyMoney/MyMoney.csv");

        DatabaseHelper dbHelper = new DatabaseHelper(context);

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
                    message.append(val).append(" | ");
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
                Account defAccount = new Account(EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Account.class),
                        account, BigDecimal.ZERO, MaterialColours.getBudgetColors().get(0));
                AccountManager.ACCOUNT_MANAGER.insertAccount(defAccount);
                accountsByName.put(defAccount.getName(), defAccount);
                account = defAccount.getId();
                Log. i("TransactionParser", "Error converting account: Account not found {AccountId" + account + "} created default");
            }

            if(budgetsByName.containsKey(budget)) {
                budget = budgetsByName.get(budget).getId();
            } else {
                Budget defBudget = new Budget(EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Budget.class),
                        budget, BigDecimal.ZERO, MaterialColours.getBudgetColors().get(0),
                        1, TimeUnit.Month, new DateTime(2000, 1, 1, 0, 0));
                BudgetManager.BUDGET_MANAGER.insertBudget(defBudget);
                budgetsByName.put(defBudget.getName(), defBudget);
                budget = defBudget.getId();
                Log. i("TransactionParser", "Error converting budget: Budget not found {BudgetId" + budget + "} created default");
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
                String frequency[] = values[8].split(" ");
                String end = values[9];
                int freq = Integer.parseInt(frequency[0]);
                TimeUnit unit = TimeUnit.valueOf(frequency[1]);
                DateTime enddate = DateTime.parse(end, Utils.getDateFormatterEU());

                t.setRecurrent(true);
                t.setFrequency(freq);
                t.setFrequencyUnit(unit);
                t.setEndDate(enddate);
            }
            return t;
        } catch (Exception e) {
            Log.e("TransactionParser", "Exception converting account", e);
        }

        return null;
    }

}
