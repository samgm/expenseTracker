package com.antso.expenses.statistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antso.expenses.R;
import com.antso.expenses.comapt.NonSwipeableViewPager;

import java.util.List;

public class StatisticsPagerHostFragment extends Fragment {
    private StatisticsPagerAdapter mStatisticsPagerAdapter = null;

    public StatisticsPagerHostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics_pagerer_host_fragment, container, false);

        if (mStatisticsPagerAdapter == null) {
            mStatisticsPagerAdapter = new StatisticsPagerAdapter(getActivity(), this.getChildFragmentManager());
        }

        NonSwipeableViewPager mPagerView = (NonSwipeableViewPager) view.findViewById(R.id.statisticsPager);
        mPagerView.setAdapter(mStatisticsPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.statisticPagerTabLayout);
        tabLayout.setupWithViewPager(mPagerView);

        return view;
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

}
