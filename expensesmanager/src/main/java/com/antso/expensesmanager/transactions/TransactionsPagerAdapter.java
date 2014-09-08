package com.antso.expensesmanager.transactions;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.TransactionDirection;

public class TransactionsPagerAdapter
        extends FragmentPagerAdapter
        implements ActionBar.TabListener {

    private final Context applicationContext;
    private ViewPager viewPager;

    public TransactionsPagerAdapter(final Context context, FragmentManager fm,
                                    ActionBar actionBar) {
        super(fm);

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
                return new TransactionListFragment(applicationContext, TransactionDirection.In);
            case 1:
                return new TransactionListFragment(applicationContext, TransactionDirection.Out);
            case 2:
                return new TransactionListFragment(applicationContext, TransactionDirection.Undef);
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
                return applicationContext.getString(R.string.title_transactions_revenues);
            case 1:
                return applicationContext.getString(R.string.title_transactions_expenses);
            case 2:
                return applicationContext.getString(R.string.title_transactions_transfers);
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

}
