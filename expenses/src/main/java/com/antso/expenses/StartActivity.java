package com.antso.expenses;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.antso.expenses.accounts.AccountListFragment;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.budgets.BudgetListFragment;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.enums.DrawerSection;
import com.antso.expenses.settings.SettingsActivity;
import com.antso.expenses.statistics.StatisticsPagerHostFragment;
import com.antso.expenses.transactions.TransactionManager;
import com.antso.expenses.transactions.TransactionPagerHostFragment;
import com.antso.expenses.utils.Constants;

import java.util.List;

public class StartActivity
    extends AppCompatActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private Toolbar toolbar;
    private CharSequence mTitle;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private int lastPosition = 0;

    private boolean backToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

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
                (DrawerLayout) findViewById(R.id.start_activity_drawlayout),
                toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            toolbar.setTitle(mTitle);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        switch (DrawerSection.valueOf(position)) {
            case TRANSACTIONS:
                mTitle = getTitle();
                lastPosition = position;
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
                        .replace(R.id.container, new StatisticsPagerHostFragment()).commit();
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
