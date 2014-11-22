package com.antso.expenses.transactions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TransactionsPagerAdapter
        extends FragmentPagerAdapter {

    public TransactionsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ExpensesListFragment();
            case 1:
                return new TransfersListFragment();
            case 2:
                return new RevenuesListFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
