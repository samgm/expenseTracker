package com.antso.expenses.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.antso.expenses.AboutFragment;

public class StatisticsPagerAdapter
        extends FragmentPagerAdapter {

    public StatisticsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AccountsStatisticsFragment();
            case 1:
                return new BudgetsStatisticsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
