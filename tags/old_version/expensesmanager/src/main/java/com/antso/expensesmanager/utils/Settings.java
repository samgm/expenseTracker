package com.antso.expensesmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    static public String getDefaultAccountId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accounts_list", null);
    }

    static public String getDefaultBudgetId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("budgets_list", null);
    }

    static public String getCurrencySymbol(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("currency_symbol", null);
    }

    public static boolean getUseDividersInTransactionList(Context applicationContext) {
        return false;
    }
}
