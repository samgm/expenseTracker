package com.antso.expenses.transactions;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antso.expenses.R;

import java.util.List;

public class TransactionPagerHostFragment extends Fragment implements ActionBar.TabListener  {
    private volatile ViewPager mPagerView;
    private TransactionsPagerAdapter mTransactionsPagerAdapter = null;
    private Context context;

    public TransactionPagerHostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        context = activity.getApplicationContext();

        View view = inflater.inflate(R.layout.transaction_pagerer_host_fragment, container, false);

        if (mTransactionsPagerAdapter == null) {
            mTransactionsPagerAdapter = new TransactionsPagerAdapter(this.getChildFragmentManager());
        }

        mPagerView = (ViewPager) view.findViewById(R.id.pager);
        mPagerView.setAdapter(mTransactionsPagerAdapter);
        mPagerView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (activity.getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
                    activity.getActionBar().setSelectedNavigationItem(position);
                }
            }
        });

        activity.getActionBar().removeAllTabs();
        for (int i = 0; i < mTransactionsPagerAdapter.getCount(); i++) {
            activity.getActionBar().addTab(activity.getActionBar().newTab()
                            .setText(this.getPageTitle(i))
                            .setTabListener(this));
        }

//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                activity.getActionBar().setSelectedNavigationItem(0);
//            }
//        });

        return view;
    }

    public CharSequence getPageTitle(int position) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //This is required because of a bug of the support library that doesn't notify nested fragments;
        //This also required to call getActivity.startActivityForResult instead of just
        // //startActivityForResult to avoid getting shifted resultCode
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (mPagerView.getCurrentItem() != tab.getPosition()) {
            mPagerView.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

}
