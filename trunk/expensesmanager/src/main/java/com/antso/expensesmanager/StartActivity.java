package com.antso.expensesmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.antso.expensesmanager.accounts.AccountListFragment;
import com.antso.expensesmanager.transactions.TransactionsPagerAdapter;
import com.antso.expensesmanager.utils.PlaceholderFragment;

public class StartActivity
    extends Activity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks, ViewPager.OnPageChangeListener {

    private CharSequence mTitle;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ViewPager mPagerView;
    private FrameLayout mContainer;

    private TransactionsPagerAdapter mTransactionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

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
        mPagerView.setOnPageChangeListener(this);

        mTransactionsPagerAdapter.setViewPager(mPagerView);
        onNavigationDrawerItemSelected(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);

        switch(mNavigationDrawerFragment.getCurrentSelectedPosition()) {
            case 0:
            case 1:
                return currentFragment.onOptionsItemSelected(item);
            case 2:
            default:
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                int id = item.getItemId();
                if (id == R.id.action_settings) {
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
            switch (mNavigationDrawerFragment.getCurrentSelectedPosition()) {
                case 0:
                case 1:
                    currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
                    break;
                case 2:
                default:
                    getMenuInflater().inflate(R.menu.menu_start_activity, menu);
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

        switch (position) {
            case 0:
                mContainer.setVisibility(View.INVISIBLE);
                mPagerView.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                getActionBar().setSelectedNavigationItem(0);
                getFragmentManager().beginTransaction()
                    .replace(R.id.container, mTransactionsPagerAdapter.getItem(0))
                    .commit();
                break;
            case 1:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fragmentManager.beginTransaction()
                    .replace(R.id.container, new AccountListFragment(getApplicationContext()))
                    .commit();
                break;
            case 2:
                mPagerView.setVisibility(View.INVISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;

                //Start new activity sample
                // mPagerView.setAdapter(null);
                // Intent intent = new Intent(this, TransactionEntryActivity.class);
                // startActivity(intent);

                //Change fragment sample
                // if (mPagerView != null) {
                //     mPagerView.removeAllViews();
                // }
                //
                // fragmentManager.beginTransaction()
                //    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                //    .commit();
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mTransactionsPagerAdapter.getItem(position))
                .commit();
        getActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
