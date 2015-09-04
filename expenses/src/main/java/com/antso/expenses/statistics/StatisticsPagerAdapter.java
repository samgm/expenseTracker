package com.antso.expenses.statistics;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.antso.expenses.R;

public class StatisticsPagerAdapter
        extends FragmentPagerAdapter {

    private Context context;

    public StatisticsPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
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
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.title_statistics_accounts);
            case 1:
                return context.getString(R.string.title_statistics_budgets);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
