package com.antso.expensesmanager;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.antso.expensesmanager.accounts.AccountListFragment;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetListFragment;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.enums.DrawerSection;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.transactions.TransactionPagerHostFragment;
import com.antso.expensesmanager.utils.PlaceholderFragment;

import java.util.List;

public class StartActivity
    extends FragmentActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CharSequence mTitle;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        AccountManager.ACCOUNT_MANAGER.start(getApplicationContext());
        BudgetManager.BUDGET_MANAGER.start(getApplicationContext());
        TransactionManager.TRANSACTION_MANAGER.start(getApplicationContext());

        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.start_activity_layout));


        mContainer = (FrameLayout)findViewById(R.id.container);
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
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new TransactionPagerHostFragment()).commit();
               break;
            case ACCOUNTS:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, new AccountListFragment()).commit();
                break;
            case SETTINGS:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment()).commit();
                break;
            case BUDGETS:
                mContainer.setVisibility(View.VISIBLE);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new BudgetListFragment()).commit();
                break;
            case STATISTICS:
            case ABOUT:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        AccountManager.ACCOUNT_MANAGER.stop();
        BudgetManager.BUDGET_MANAGER.stop();
        TransactionManager.TRANSACTION_MANAGER.stop();

        super.onDestroy();
    }
}
