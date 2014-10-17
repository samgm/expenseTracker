package com.antso.expensesmanager;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antso.expensesmanager.accounts.AccountListFragment;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetListFragment;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.enums.DrawerSection;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.transactions.TransactionPagerHostFragment;
import com.antso.expensesmanager.utils.Constants;

import java.util.List;

public class StartActivity
    extends FragmentActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CharSequence mTitle;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private int lastPosition = 0;

    private boolean backToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        final ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar);
        progress.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                TransactionManager.TRANSACTION_MANAGER().start(getApplicationContext());
                AccountManager.ACCOUNT_MANAGER().start(getApplicationContext());
                BudgetManager.BUDGET_MANAGER().start(getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progress.setVisibility(View.GONE);
            }
        }.execute();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.start_activity_layout));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //This is required because of a bug of the support library that doesn't notify nested fragments;
        //This also required to call getActivity.startActivityForResult instead of just
        //startActivityForResult to avoid getting shifted resultCode
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        if (requestCode == Constants.START_SETTINGS_ACTIVITY_REQUEST_CODE) {
            mNavigationDrawerFragment.selectItem(lastPosition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        switch (DrawerSection.valueOf(position)) {
            case TRANSACTIONS:
                mTitle = getTitle();
                lastPosition = position;
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new TransactionPagerHostFragment()).commit();
               break;
            case ACCOUNTS:
                mTitle = getText(R.string.title_accounts_list_section);
                lastPosition = position;
                fragmentManager.beginTransaction()
                    .replace(R.id.container, new AccountListFragment()).commit();
                break;
            case BUDGETS:
                mTitle = getText(R.string.title_budgets_list_section);
                lastPosition = position;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new BudgetListFragment()).commit();
                break;
            case STATISTICS:
                mTitle = getText(R.string.title_statistics_section);
                lastPosition = position;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new StatisticsFragment())
                        .commit();
                break;
            case SETTINGS:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        Constants.START_SETTINGS_ACTIVITY_REQUEST_CODE);
                break;
            case ABOUT:
                mTitle = getText(R.string.title_about_section);
                lastPosition = position;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new AboutFragment())
                        .commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (backToExitPressedOnce) {
            AccountManager.ACCOUNT_MANAGER().stop();
            BudgetManager.BUDGET_MANAGER().stop();
            TransactionManager.TRANSACTION_MANAGER().stop();

            super.onBackPressed();
            return;
        }

        backToExitPressedOnce = true;
        Toast.makeText(this, R.string.message_click_again_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backToExitPressedOnce = false;
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
