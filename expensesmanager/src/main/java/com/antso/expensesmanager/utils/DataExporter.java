package com.antso.expensesmanager.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.utils.csv.CSVReader;
import com.antso.expensesmanager.utils.csv.CSVWriter;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataExporter {

    private final Context context;
    private final String budgetFilePrefix = "budgets_export_";
    private final String accountFilePrefix = "accounts_export_";
    private final String transactionFilePrefix = "transactions_export_";
    private final String appFolderPath;


    public DataExporter(final Context context) {
        this.context = context;
        String appFolder = context.getText(R.string.app_data_folder).toString();
        appFolderPath = String.valueOf(Environment.getExternalStorageDirectory()) + "/" + appFolder;
    }

    public void exportData() {
        File appFolder = new File(appFolderPath);
        if (!appFolder.exists()) {
            appFolder.mkdir();
        }
        exportTransactionData();
        exportAccountData();
        exportBudgetData();
    }

    private void exportTransactionData() {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(transactionFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
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
        }
    }

    private void exportAccountData() {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(accountFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
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
        }
    }

    private void exportBudgetData() {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/" + "budgets_export_");
        filePath.append(Utils.dateTimeToyyyyMMdd(DateTime.now()));
        filePath.append(".csv");

        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to write data", e);
            return;
        }

        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Collection<Budget> budgets = dbHelper.getBudgets();
        for (Budget b : budgets) {
            String[] bString = new String[7];

            bString[0] = b.getId();
            bString[1] = b.getName();
            bString[2] = b.getThreshold().setScale(2).toPlainString();
            bString[3] = String.valueOf(b.getColor());
            bString[4] = String.valueOf(b.getPeriodLength());
            bString[5] = b.getPeriodUnit().getStringValue();
            bString[6] = String.valueOf(Utils.dateTimeToyyyyMMdd(b.getPeriodStart()));

            writer.writeNext(bString);
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised closing file to write data", e);
        }
    }

    public void importData(String importDate) {
        File appFolder = new File(appFolderPath);
        if (!appFolder.exists()) {
            return;
        }

        importAccountData(importDate);
        importBudgetData(importDate);
        importTransactionData(importDate);
    }

    private void importTransactionData(String importDate) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(transactionFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to read data", e);
            return;
        }

        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        try {
            String[] values = reader.readNext();
            while (values != null) {
                logReadValues(values);
                Transaction t = new Transaction(
                        values[0],
                        values[1],
                        TransactionDirection.valueOf(values[2]),
                        TransactionType.valueOf(values[3]),
                        values[4],
                        values[5],
                        BigDecimal.valueOf(Double.parseDouble(values[6])),
                        Utils.yyyyMMddToDate(Integer.parseInt(values[7])));

                t.setLinkedTransactionId(values[8]);
                t.setRecurrent(Boolean.parseBoolean(values[9]));
                t.setFrequency(Integer.parseInt(values[10]));
                t.setFrequencyUnit(TimeUnit.valueOf(values[11]));
                t.setEndDate(Utils.yyyyMMddToDate(Integer.parseInt(values[12])));
                t.setRepetitionNum(Integer.parseInt(values[13]));

                dbHelper.insertTransactions(t);

                values = reader.readNext();
            }
        } catch (IOException e) {
            Log.e("DataImport", "Exception raised importing transactions", e);
        }
    }

    private void importAccountData(String importDate) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(accountFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to read data", e);
            return;
        }

        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        try {
            String[] values = reader.readNext();
            while (values != null) {
                logReadValues(values);
                Account a = new Account(
                        values[0],
                        values[1],
                        BigDecimal.valueOf(Double.parseDouble(values[2])),
                        Integer.parseInt(values[3]));

                dbHelper.insertAccount(a);

                values = reader.readNext();
            }
        } catch (IOException e) {
            Log.e("DataImport", "Exception raised importing account", e);
        }
    }

    private void importBudgetData(String importDate) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(budgetFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath.toString());
        } catch (IOException e) {
            Log.e("DataExport", "Exception raised opening file to read data", e);
            return;
        }

        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        try {
            String[] values = reader.readNext();
            while (values != null) {
                logReadValues(values);
                Budget b = new Budget(
                        values[0],
                        values[1],
                        BigDecimal.valueOf(Double.parseDouble(values[2])),
                        Integer.parseInt(values[3]),
                        Integer.parseInt(values[4]),
                        TimeUnit.valueOf(values[5]),
                        Utils.yyyyMMddToDate(Integer.parseInt(values[6])));

                dbHelper.insertBudget(b);

                values = reader.readNext();
            }
        } catch (IOException e) {
            Log.e("DataImport", "Exception raised importing account", e);
        }
    }

    private void logReadValues(String[] values) {
        StringBuilder message = new StringBuilder();
        for (String val : values) {
            message.append(val).append(" | ");
        }
        Log.i("CVSReader", message.toString());
    }

    public String[] getDataImports() {
        File appFolder = new File(appFolderPath);
        if (!appFolder.exists()) {
            return new String[0];
        }

        Map<String, Integer> dates = new HashMap<String, Integer>();
        File f = new File(appFolderPath);
        File files[] = f.listFiles();
        for (File file : files) {
            String name = file.getName();
            if(name.contains(accountFilePrefix)) {
                name = name.replace(accountFilePrefix, "").replace(".csv", "");
                int i = (dates.containsKey(name)) ? dates.get(name) + 1 : 1;
                dates.put(name, i);
            } else  if(name.contains(budgetFilePrefix)) {
                name = name.replace(budgetFilePrefix, "").replace(".csv", "");
                int i = (dates.containsKey(name)) ? dates.get(name) + 1 : 1;
                dates.put(name, i);
            } else  if(name.contains(transactionFilePrefix)) {
                name = name.replace(transactionFilePrefix, "").replace(".csv", "");
                int i = (dates.containsKey(name)) ? dates.get(name) + 1 : 1;
                dates.put(name, i);
            }
        }

        ArrayList<String> result = new ArrayList<String>();
        for (String date : dates.keySet()) {
            if (dates.get(date) == 3) {
                result.add(date);
            }
        }

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return result.toArray(new String[0]);
    }
}
