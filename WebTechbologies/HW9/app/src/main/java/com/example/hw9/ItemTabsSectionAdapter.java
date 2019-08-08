package com.example.hw9;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemTabsSectionAdapter extends FragmentPagerAdapter {

    private final List<Fragment> tabFragment = new ArrayList<>();
    private final List<String> tabFragmentTitle = new ArrayList<>();

    public ItemTabsSectionAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        tabFragment.add(fragment);
        tabFragmentTitle.add(title);
    }

    @Override
    public CharSequence getPageTitle(int index) {
        return tabFragmentTitle.get(index);
    }

    @Override
    public Fragment getItem(int index) {
        return tabFragment.get(index);
    }

    @Override
    public int getCount() {
        return tabFragment.size();
    }
}
