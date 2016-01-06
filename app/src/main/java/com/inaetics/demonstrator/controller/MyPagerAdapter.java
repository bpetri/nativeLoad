package com.inaetics.demonstrator.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.inaetics.demonstrator.fragments.ConsoleFragment;
import com.inaetics.demonstrator.fragments.OsgiBundlesFragment;

/**
 * Created by mjansen on 17-9-15.
 * FragmentPagerAdapter so you can swipe to a other fragment
 */
public class MyPagerAdapter extends FragmentPagerAdapter {
    private final String[] TITLES = {"Bundles", "Console"};

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    /**
     * Method called by pager
     * @param position      0 = leftfragment, 1 = right fragment
     * @return              Fragment that should be on this position.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new OsgiBundlesFragment();
            case 1:
                return new ConsoleFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }
}
