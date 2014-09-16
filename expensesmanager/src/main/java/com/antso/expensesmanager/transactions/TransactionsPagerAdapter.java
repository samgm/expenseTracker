package com.antso.expensesmanager.transactions;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.antso.expensesmanager.R;

public class TransactionsPagerAdapter
        extends FragmentPagerAdapter
        implements ActionBar.TabListener {

    private final Context applicationContext;
    private ViewPager viewPager;

    private Fragment currentRevenueFragment;
    private Fragment currentExpensesFragment;
    private Fragment currentTransferFragment;

    public TransactionsPagerAdapter(final Context context, FragmentManager fragmentManager,
                                    ActionBar actionBar) {
        super(fragmentManager);

        this.applicationContext = context;

        for (int i = 0; i < this.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                            .setText(this.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                currentExpensesFragment = new ExpensesListFragment();
                return currentExpensesFragment;
            case 1:
                currentTransferFragment = new TransfersListFragment();
                return currentTransferFragment;
            case 2:
                currentRevenueFragment = new RevenuesListFragment();
                return currentRevenueFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return applicationContext.getString(R.string.title_transactions_expenses);
            case 1:
                return applicationContext.getString(R.string.title_transactions_transfers);
            case 2:
                return applicationContext.getString(R.string.title_transactions_revenues);
        }
        return null;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public Fragment getCurrentFragment(int position) {
        switch (position) {
            case 0:
                if(currentExpensesFragment == null) {
                    return getItem(position);
                }
                return currentExpensesFragment;
            case 1:
                if(currentTransferFragment == null) {
                    return getItem(position);
                }
                return currentTransferFragment;
            case 2:
                if(currentRevenueFragment == null) {
                    return getItem(position);
                }
                return currentRevenueFragment;
        }
        return null;    }
}
