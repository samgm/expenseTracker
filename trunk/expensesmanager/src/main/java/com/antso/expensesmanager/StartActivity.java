package com.antso.expensesmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.antso.expensesmanager.accounts.AccountListFragment;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetListFragment;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.enums.DrawerSection;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.transactions.TransactionManager;
import com.antso.expensesmanager.transactions.TransactionsPagerAdapter;
import com.antso.expensesmanager.utils.PlaceholderFragment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;

public class StartActivity
    extends Activity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CharSequence mTitle;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ViewPager mPagerView;
    private FrameLayout mContainer;

    private TransactionsPagerAdapter mTransactionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        AccountManager.ACCOUNT_MANAGER.start(getApplicationContext());
        BudgetManager.BUDGET_MANAGER.start(getApplicationContext());
        TransactionManager.TRANSACTION_MANAGER.start(getApplicationContext());

        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.start_activity_layout));

        mTransactionsPagerAdapter = new TransactionsPagerAdapter(getApplicationContext(),
                getFragmentManager(), getActionBar());

        mContainer = (FrameLayout)findViewById(R.id.container);

        mPagerView = (ViewPager)findViewById(R.id.pager);
        mPagerView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
                    getActionBar().setSelectedNavigationItem(position);
                }
            }
        });
        mPagerView.setAdapter(mTransactionsPagerAdapter);

        mTransactionsPagerAdapter.setViewPager(mPagerView);
        onNavigationDrawerItemSelected(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (DrawerSection.valueOf(mNavigationDrawerFragment.getCurrentSelectedPosition())) {
            case TRANSACTIONS: {
                Fragment currentFragment = mTransactionsPagerAdapter.getCurrentFragment(
                        getActionBar().getSelectedNavigationIndex());
                return currentFragment.onOptionsItemSelected(item);
            }
            case ACCOUNTS:
            case BUDGETS:
            case STATISTICS:
            case SETTINGS:
            case ABOUT: {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
                return currentFragment.onOptionsItemSelected(item);
            }
//                // Handle action bar item clicks here. The action bar will
//                // automatically handle clicks on the Home/Up button, so long
//                // as you specify a parent activity in AndroidManifest.xml.
//                int id = item.getItemId();
//                if (id == R.id.action_settings) {
//                    return true;
//                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            switch (DrawerSection.valueOf(mNavigationDrawerFragment.getCurrentSelectedPosition())) {
                case TRANSACTIONS: {
                    Fragment currentFragment = mTransactionsPagerAdapter.getCurrentFragment(getActionBar().getSelectedNavigationIndex());
                    currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
                } break;
                case ACCOUNTS:
                case BUDGETS:
                case STATISTICS:
                case SETTINGS:
                case ABOUT: {
                    Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
                    currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
//                    getMenuInflater().inflate(R.menu.menu_start_activity, menu);
                }
            }

            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (mPagerView == null) {
            return;
        }

        switch (DrawerSection.valueOf(position)) {
            case TRANSACTIONS:
                mContainer.setVisibility(View.INVISIBLE);
                mPagerView.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                getActionBar().setSelectedNavigationItem(0);
                break;
            case ACCOUNTS:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fragmentManager.beginTransaction()
                    .replace(R.id.container, new AccountListFragment()).commit();
                break;
            case SETTINGS:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment()).commit();
                break;
            case BUDGETS:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new BudgetListFragment()).commit();
                break;
            case STATISTICS:
            case ABOUT:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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
