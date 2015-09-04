package com.antso.expenses.transactions;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.antso.expenses.R;
import com.antso.expenses.comapt.FloatingActionButtonClicked;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.utils.Constants;
import com.antso.expenses.utils.IntentParamNames;

import java.util.List;

public class TransactionPagerHostFragment extends Fragment {
    private TransactionsPagerAdapter mTransactionsPagerAdapter = null;
    private volatile int currentFragmentIndex = 0;

    public TransactionPagerHostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transaction_pagerer_host_fragment, container, false);

        FloatingActionButton myFab = (FloatingActionButton)view.findViewById(R.id.addFloatingButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionDirection direction = TransactionDirection.Undef;
                TransactionType type = TransactionType.Undef;
                switch (currentFragmentIndex) {
                    case 0: //expense
                        direction = TransactionDirection.Out;
                        break;
                    case 1: //transfer
                        type = TransactionType.Transfer;
                        break;
                    case 2: //revenue
                        direction = TransactionDirection.In;
                        break;
                }
                Intent intent = new Intent(getActivity().getApplicationContext(), TransactionEntryActivity.class);
                if (!direction.equals(TransactionDirection.Undef)) {
                    intent.putExtra(IntentParamNames.TRANSACTION_DIRECTION, direction.getIntValue());
                }
                if (!type.equals(TransactionType.Undef)) {
                    intent.putExtra(IntentParamNames.TRANSACTION_TYPE, type.getIntValue());
                }
                startActivityForResult(intent, Constants.TRANSFER_TRANSACTION_ENTRY_REQUEST_CODE);
            }
        });

        if (mTransactionsPagerAdapter == null) {
            mTransactionsPagerAdapter = new TransactionsPagerAdapter(getActivity(), this.getChildFragmentManager());
        }

        ViewPager mPagerView = (ViewPager) view.findViewById(R.id.transactionPager);
        mPagerView.setAdapter(mTransactionsPagerAdapter);
        mPagerView.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentFragmentIndex = position;
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.transactionPagerTabLayout);
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
