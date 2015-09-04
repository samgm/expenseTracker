package com.antso.expenses.transactions;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.antso.expenses.R;

public class TransactionsPagerAdapter
        extends FragmentPagerAdapter {

    private Context context;

    public TransactionsPagerAdapter(Context context,
                                    FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
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

    @Override
    public CharSequence getPageTitle (int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.title_transactions_expenses);
            case 1:
                return context.getString(R.string.title_transactions_transfers);
            case 2:
                return context.getString(R.string.title_transactions_revenues);
        }
        return null;
    }
}
