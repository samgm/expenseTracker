package com.antso.expenses.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.antso.expenses.R;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.persistence.DatabaseHelper;
import com.antso.expenses.utils.csv.CSVReader;
import com.antso.expenses.utils.csv.CSVWriter;

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
    private final String settingsFilePrefix = "settings_export_";
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
        try {
            exportTransactionData();
            exportAccountData();
            exportBudgetData();
            exportSettingData();
        } catch (Exception e) {
            Log.e("DataExporter", "Exception exporting data: " + e.getMessage());
            notifyExportErrorToUIThread(e.getMessage());
        }
    }

    private void exportTransactionData() throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(transactionFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(Utils.now()));
        filePath.append(".csv");

        FileWriter fileWriter = new FileWriter(filePath.toString());
        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Collection<Transaction> transactions = dbHelper.getTransactions();
        for (Transaction t : transactions) {
            String[] tString = new String[15];
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
            tString[14] = t.getFeeTransactionId();

            writer.writeNext(tString);
        }

        writer.flush();
        writer.close();
    }

    private void exportAccountData() throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(accountFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(Utils.now()));
        filePath.append(".csv");

        FileWriter fileWriter = new FileWriter(filePath.toString());
        CSVWriter writer = new CSVWriter(fileWriter);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Collection<Account> accounts = dbHelper.getAccounts();
        for (Account a : accounts) {
            String[] aString = new String[5];

            aString[0] = a.getId();
            aString[1] = a.getName();
            aString[2] = a.getInitialBalance().setScale(2).toPlainString();
            aString[3] = String.valueOf(a.getColor());
            aString[4] = String.valueOf(a.isArchived());

            writer.writeNext(aString);
        }

        writer.flush();
        writer.close();
    }

    private void exportBudgetData() throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(budgetFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(Utils.now()));
        filePath.append(".csv");

        FileWriter fileWriter = new FileWriter(filePath.toString());
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

        writer.flush();
        writer.close();
    }

    private void exportSettingData() throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(settingsFilePrefix);
        filePath.append(Utils.dateTimeToyyyyMMdd(Utils.now()));
        filePath.append(".csv");

        FileWriter fileWriter = new FileWriter(filePath.toString());
        CSVWriter writer = new CSVWriter(fileWriter);
        Map<String, ?> settings = Settings.getAllSettings(context);
        for (String name : settings.keySet()) {
            Object value = settings.get(name);
            String[] sString = new String[3];
            sString[0] = name;
            sString[1] = value.getClass().getName();
            sString[2] = value.toString();

            writer.writeNext(sString);
        }

        writer.flush();
        writer.close();
    }

    public void importData(String importDate) {
        File appFolder = new File(appFolderPath);
        if (!appFolder.exists()) {
            return;
        }

        try {
            importAccountData(importDate);
            importBudgetData(importDate);
            importTransactionData(importDate);
            importSettingData(importDate);
        } catch (Exception e) {
            Log.e("DataExporter", "Exception exporting data: " + e.getMessage());
            notifyImportErrorToUIThread(e.getMessage());
        }
    }

    private void importTransactionData(String importDate) throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(transactionFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader = new FileReader(filePath.toString());
        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

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
            if (values.length >= 15 && values[14] != null) {
                t.setFeeTransactionId(values[14]);
            }

            dbHelper.insertTransactions(t);

            values = reader.readNext();
        }
    }

    private void importAccountData(String importDate) throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(accountFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader = new FileReader(filePath.toString());
        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        String[] values = reader.readNext();
        while (values != null) {
            logReadValues(values);
            Account a = new Account(
                    values[0],
                    values[1],
                    BigDecimal.valueOf(Double.parseDouble(values[2])),
                    Integer.parseInt(values[3]),
                    Boolean.parseBoolean(values[4]));

            dbHelper.insertAccount(a);

            values = reader.readNext();
        }
    }

    private void importBudgetData(String importDate) throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(budgetFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader = new FileReader(filePath.toString());
        CSVReader reader = new CSVReader(fileReader);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

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
    }

    private void importSettingData(String importDate) throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(appFolderPath);
        filePath.append("/").append(settingsFilePrefix);
        filePath.append(importDate);
        filePath.append(".csv");

        FileReader fileReader = new FileReader(filePath.toString());
        CSVReader reader = new CSVReader(fileReader);
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

        String[] values = reader.readNext();
        while (values != null) {
            logReadValues(values);
            String name = values[0];
            String className = values[1];
            String value = values[2];

            switch (className) {
                case "java.lang.String":
                    preferences.putString(name, value);
                    break;
                case "java.lang.Boolean":
                    preferences.putBoolean(name, Boolean.parseBoolean(value));
                    break;
                case "java.lang.int":
                    preferences.putInt(name, Integer.valueOf(value));
                    break;

            }

            values = reader.readNext();
        }

        preferences.commit();
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

    private void notifyExportErrorToUIThread(final String message) {
        notifyErrorToUIThread(R.string.error_exporting_data, message);
    }

    private void notifyImportErrorToUIThread(final String message) {
        notifyErrorToUIThread(R.string.error_importing_data, message);
    }

    private void notifyErrorToUIThread(final int message, final String details) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.title_error_dialog)
                        .setMessage(context.getText(message) + " " + details)
                        .setPositiveButton(R.string.got_it, null)
                        .create();
                dialog.show();
            }
        });
    }

}
