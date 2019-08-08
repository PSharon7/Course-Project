package com.example.hw9;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ProductSectionAdapter productSectionAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productSectionAdapter = new ProductSectionAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ProductSectionAdapter psAdapter = new ProductSectionAdapter(getSupportFragmentManager());
        psAdapter.addFragment(new ProductSearchFragment(), "SEARCH");
        psAdapter.addFragment(new ProductWishListFragment(), "WISH LIST");
        viewPager.setAdapter(psAdapter);
    }
}
