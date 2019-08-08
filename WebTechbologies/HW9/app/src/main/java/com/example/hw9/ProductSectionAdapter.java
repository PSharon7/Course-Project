package com.example.hw9;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductSectionAdapter extends FragmentPagerAdapter {

    private final List<Fragment> productFragment = new ArrayList<>();
    private final List<String> productFragmentTitle = new ArrayList<>();

    public ProductSectionAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        productFragment.add(fragment);
        productFragmentTitle.add(title);
    }

    @Override
    public CharSequence getPageTitle(int index) {
        return productFragmentTitle.get(index);
    }

    @Override
    public Fragment getItem(int index) {
        return productFragment.get(index);
    }

    @Override
    public int getCount() {
        return productFragment.size();
    }
}
