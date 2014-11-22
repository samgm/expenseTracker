package com.antso.expensesmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.utils.BaseAsyncTaskWithProgress;
import com.antso.expensesmanager.utils.DataExporter;
import com.antso.expensesmanager.views.SpinnerChooserDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // Add 'general' preferences
        //noinspection deprecation
        addPreferencesFromResource(R.xml.pref_general);

        //Account list
        ArrayList<String> accountIds = new ArrayList<String>();
        ArrayList<String> accountNames = new ArrayList<String>();
        for (Account account : AccountManager.ACCOUNT_MANAGER().getAccounts()) {
            accountIds.add(account.getId());
            accountNames.add(account.getName());
        }
        //noinspection deprecation
        ListPreference accountsList = (ListPreference) findPreference("accounts_list");
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        accountsList.setEntries(accountNames.toArray(new String[0]));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        accountsList.setEntryValues(accountIds.toArray(new String[0]));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        accountsList.setDefaultValue(accountIds.toArray(new String[0])[0]);

        //Budget list
        ArrayList<String> budgetIds = new ArrayList<String>();
        ArrayList<String> budgetNames = new ArrayList<String>();
        for (Budget budget : BudgetManager.BUDGET_MANAGER().getBudgets()) {
            budgetIds.add(budget.getId());
            budgetNames.add(budget.getName());
        }
        //noinspection deprecation
        ListPreference budgetList = (ListPreference) findPreference("budgets_list");
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        budgetList.setEntries(budgetNames.toArray(new String[0]));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        budgetList.setEntryValues(budgetIds.toArray(new String[0]));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        budgetList.setDefaultValue(accountIds.toArray(new String[0])[0]);

        //Currency Symbol
        //noinspection deprecation
        EditTextPreference currencySymbol = (EditTextPreference) findPreference("currency_symbol");
        currencySymbol.setDefaultValue("$");

        // Add 'data import and export' preferences
        PreferenceCategory header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_data_import_export);
        //noinspection deprecation
        getPreferenceScreen().addPreference(header);
        //noinspection deprecation
        addPreferencesFromResource(R.xml.pref_data_import_export);

        //noinspection deprecation
        final Preference importData = findPreference("import_data");
        importData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                importData();
                return true;
            }
        });

        //noinspection deprecation
        Preference exportData = findPreference("export_data");
        exportData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportData();
                return true;
            }
        });

//        // Add 'notifications' preferences, and a corresponding header.
//        PreferenceCategory fakeHeader = new PreferenceCategory(this);
//        fakeHeader.setTitle(R.string.pref_header_notifications);
//        getPreferenceScreen().addPreference(fakeHeader);
//        addPreferencesFromResource(R.xml.pref_notification);

        //noinspection deprecation
        bindPreferenceSummaryToValue(findPreference("accounts_list"));
        //noinspection deprecation
        bindPreferenceSummaryToValue(findPreference("budgets_list"));
        //noinspection deprecation
        bindPreferenceSummaryToValue(findPreference("currency_symbol"));
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            bindPreferenceSummaryToValue(findPreference("accounts_list"));
            bindPreferenceSummaryToValue(findPreference("budgets_list"));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataImportExportPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_import_export);

            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class NotificationPreferenceFragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.pref_notification);
//
//            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
//        }
//    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private void importData() {
        final DataExporter exporter = new DataExporter(SettingsActivity.this);
        final String[] imports = exporter.getDataImports();
        if (imports.length == 0) {
            AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle(R.string.title_sorry_dialog)
                    .setMessage(R.string.message_nothing_to_import_dialog)
                    .setNeutralButton(R.string.got_it, null)
                    .create();
            dialog.show();
            return;
        }

        if (imports.length == 1) {
            confirmAndImport(exporter, imports[0]);
            return;
        }

        final SpinnerChooserDialog importChooser = new SpinnerChooserDialog(
                R.string.title_import_chooser_dialog,
                R.string.message_import_chooser_dialog,
                this,
                new SpinnerChooserDialog.OnDialogDismissed() {
                    @Override
                    public void onDismissed(boolean confirm, final String selectedDate) {
                        if(!confirm) {
                            return;
                        }
                        confirmAndImport(exporter, selectedDate);
                    }
                }, imports);
        importChooser.show();
    }

    private void confirmAndImport(final DataExporter exporter, final String selectedDate) {
        final DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.title_confirm_dialog)
                .setMessage(R.string.message_existing_all_data_will_be_deleted)
                .setPositiveButton(R.string.sure_go_on, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new BaseAsyncTaskWithProgress<Void>(SettingsActivity.this, R.string.working) {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        BudgetManager.BUDGET_MANAGER().stop();
                                        AccountManager.ACCOUNT_MANAGER().stop();
                                        TransactionManager.TRANSACTION_MANAGER().stop();

                                        dbHelper.deleteDatabase();
                                        exporter.importData(selectedDate);

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

    private void exportData() {
        new BaseAsyncTaskWithProgress<Void>(SettingsActivity.this, R.string.working) {
            @Override
            protected Void doInBackground(Void... params) {
                DataExporter exporter = new DataExporter(SettingsActivity.this);
                exporter.exportData();
                return null;
            }
        }.execute();
    }
}