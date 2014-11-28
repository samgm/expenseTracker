package com.antso.expenses.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    static public String getCurrencySymbol(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("currency_symbol", null);
    }

    static public String getDefaultExpenseAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accounts_list_expenses", null);
    }

    static public String getDefaultRevenueAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accounts_list_revenues", null);
    }

    static public String getDefaultTransferFromAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accounts_list_transfer_from", null);
    }

    static public String getDefaultTransferToAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accounts_list_transfer_to", null);
    }

    static public String getDefaultExpenseBudgetId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("budgets_list_expenses", null);
    }

    static public String getDefaultRevenueBudgetId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("budgets_list_revenues", null);
    }

    static public String getDefaultTransferBudgetId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("budgets_list_transfer", null);
    }

    public static boolean getUseDividersInTransactionList(Context applicationContext) {
        return false;
    }
}
