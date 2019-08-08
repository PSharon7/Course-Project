package com.example.hw9;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";

    private ItemTabsSectionAdapter itemTabsSectionAdapter;
    private ViewPager viewPager;

    private FloatingActionButton wishlistBtn;
    private SharedPreferences wishList;
    private SharedPreferences.Editor wishListEditor;

    String itemId, title, itemShipping, shippingInfo, itemDetail;
    JSONObject itemDetailObj;
    String itemUrl;

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_tabs);


        itemId = getIntent().getStringExtra("itemId");
        title = getIntent().getStringExtra("itemTitle");
        itemShipping = getIntent().getStringExtra("itemShipping");
        shippingInfo = getIntent().getStringExtra("itemShippingInfo");
        itemDetail = getIntent().getStringExtra("itemDetail");

        try {
            itemDetailObj = new JSONObject(itemDetail);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }


        Toolbar backToolbar = findViewById(R.id.item_toolbar);
        setSupportActionBar(backToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);

        itemTabsSectionAdapter = new ItemTabsSectionAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.tab_container);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.item_tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        // Wish list
        wishList = PreferenceManager.getDefaultSharedPreferences(this);
        wishListEditor = wishList.edit();
        wishlistBtn = findViewById(R.id.wishList_btn);

        if (wishList.contains(itemId)) {
            wishlistBtn.setImageResource(R.drawable.cart_remove_btn);
        } else {
            wishlistBtn.setImageResource(R.drawable.cart_plus_btn);
        }

        wishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wishList.contains(itemId)) {
                    //add
                    Toast.makeText(getApplicationContext(), title + " was added to wish list", Toast.LENGTH_LONG).show();

                    wishListEditor.putString(itemId, itemDetail);
                    wishListEditor.commit();

                    wishlistBtn.setImageResource(R.drawable.cart_remove_btn);
                } else {
                    //remove

                    Toast.makeText(getApplicationContext(), title + " was removed from wish list", Toast.LENGTH_LONG).show();

                    wishListEditor.remove(itemId);
                    wishListEditor.commit();

                    wishlistBtn.setImageResource(R.drawable.cart_plus_btn);

                }
            }
        });


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = API.BASE_URL + "/itemDetail?itemId=" + itemId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject itemObject = new JSONObject(response);
                            Log.d("response", response);

                            if (itemObject.has("ack") && itemObject.getString("ack").equals("Success")) {
                                JSONObject itemObj = itemObject.getJSONObject("item");
                                itemUrl = itemObj.getString("viewItemURL");
                            }

                        } catch (JSONException e) {
                            //ERROR
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ERROR
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(stringRequest);

    }

    private void setupViewPager(ViewPager viewPager) {
        ItemTabsSectionAdapter itemTabsSectionAdapter = new ItemTabsSectionAdapter(getSupportFragmentManager());

        itemTabsSectionAdapter.addFragment(new ItemProductFragment(), "PRODUCT");
        itemTabsSectionAdapter.addFragment(new ItemShippingFragment(), "SHIPPING");
        itemTabsSectionAdapter.addFragment(new ItemPhotoFragment(), "PHOTOS");
        itemTabsSectionAdapter.addFragment(new ItemSimilarFragment(), "SIMILAR");
        viewPager.setAdapter(itemTabsSectionAdapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.information_variant);
        tabLayout.getTabAt(1).setIcon(R.drawable.truck_delivery);
        tabLayout.getTabAt(2).setIcon(R.drawable.google);
        tabLayout.getTabAt(3).setIcon(R.drawable.equal);

        tabLayout.getTabAt(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().clearColorFilter();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Return btn in the tool bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.facebook:
                try {

                    String url = "https://www.facebook.com/dialog/share?app_id=841288729543017&hashtag=%23CSCI571Spring2019Ebay&href=" + itemUrl + "&display=popup&quote=Buy " + title + " for " + itemDetailObj.getString("price") + " from Ebay!";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                    return true;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
