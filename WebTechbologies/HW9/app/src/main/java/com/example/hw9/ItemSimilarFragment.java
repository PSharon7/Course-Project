package com.example.hw9;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemSimilarFragment extends Fragment {
    private static final String TAG = "ItemSimilarFragment";

    private ArrayList<String> itemIds = new ArrayList<>();
    private ArrayList<String> itemImages = new ArrayList<>();
    private ArrayList<String> itemTitles = new ArrayList<>();
    private ArrayList<Float> itemShippings = new ArrayList<>();
    private ArrayList<Float> itemTimeLefts = new ArrayList<>();
    private ArrayList<Float> itemPrices = new ArrayList<>();
    private ArrayList<String> itemUrls = new ArrayList<>();

    private List<JSONObject> itemList = new ArrayList<>();
    private List<JSONObject> itemListForSort = new ArrayList<>();

    View view;
    Spinner spinnerSortBy;
    Spinner spinnerOrder;
    String sortByName = "Default";
    String orderName = "Ascending";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.item_similar, container, false);

        final String itemId = ((ItemActivity) getActivity()).itemId;

        // Progress bar
        final ProgressBar progressBar = view.findViewById(R.id.tab4_progressBar);
        final TextView progressText = view.findViewById(R.id.tab4_progressText);

        // Alert
        final TextView alert = view.findViewById(R.id.tab4_alert);

        // Spinner
        spinnerSortBy = view.findViewById(R.id.spinnerSort);
        spinnerOrder = view.findViewById(R.id.spinnerOrder);
        spinnerSortBy.setEnabled(false);
        spinnerOrder.setEnabled(false);


        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] sortBy = getResources().getStringArray(R.array.tab4_sort_arr);
                sortByName = sortBy[position];

                if(sortByName.equals("Default")){
                    spinnerOrder.setEnabled(false);
                }
                else {
                    spinnerOrder.setEnabled(true);
                }

                sortDataByType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] order = getResources().getStringArray(R.array.tab4_order_arr);
                orderName = order[position];

                sortDataByType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(ItemSimilarFragment.this.getActivity());
        String url = API.BASE_URL + "/similarItemDetail?itemId="+ itemId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, response);

                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);


                            if (jsonObject.has("ack") && jsonObject.get("ack").toString().equals("Success")) {
                                spinnerSortBy.setEnabled(true);
                                JSONArray itemArray = jsonObject.getJSONArray("item");

                                for (int i = 0; i < itemArray.length(); i++) {
                                    itemList.add(itemArray.getJSONObject(i));
                                    itemListForSort.add(itemArray.getJSONObject(i));
                                }

                                setSimilarItems();

                            } else {
                                // Fail
                                alert.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e) {
                            //ERROR
                            alert.setVisibility(View.VISIBLE);
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ERROR
                Log.d(TAG, "Error in tab4");
            }
        });

        queue.add(stringRequest);
        return view;
    }


    private void setSimilarItems() {
        try {
            for (int i = 0; i < itemListForSort.size(); i++) {
                JSONObject jsonObj = itemListForSort.get(i);

                String itemId, itemImage, itemTitle, itemUrl;
                Float itemShipping, itemTimeLeft, itemPrice;

                itemId = jsonObj.getString("itemId");
                itemImage = jsonObj.getString("image");
                itemTitle = jsonObj.getString("productName");
                itemShipping = Float.valueOf(jsonObj.getString("shippingCost"));
                itemTimeLeft = Float.valueOf(jsonObj.getString("dayLeft"));
                itemPrice = Float.valueOf(jsonObj.getString("price"));
                itemUrl = jsonObj.getString("productURL");

                itemIds.add(itemId);
                itemImages.add(itemImage);
                itemTitles.add(itemTitle);
                itemShippings.add(itemShipping);
                itemTimeLefts.add(itemTimeLeft);
                itemPrices.add(itemPrice);
                itemUrls.add(itemUrl);
            }

            setRecyclerView();
        }
        catch (JSONException e) {
            //ERROR
            Log.d(TAG, "JSON Error in setItemResult");
        }
    }

    private void setRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getContext(), 1);
        RecyclerView recyclerView = view.findViewById(R.id.tab4_resultRecycler);

        ItemSimilarRecyclerAdapter recyclerAdapter = new ItemSimilarRecyclerAdapter(
                this.getContext(),
                itemIds,
                itemImages,
                itemTitles,
                itemShippings,
                itemTimeLefts,
                itemPrices,
                itemUrls);

        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(null);
        recyclerAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(recyclerAdapter);

    }

    private void sortDataByType() {
        itemListForSort = new ArrayList<>(itemList);

        if(sortByName.equals("Name")) {
            Collections.sort(itemListForSort, new NameComparator());
        }
        else if(sortByName.equals("Price")) {
            Collections.sort(itemListForSort, new PriceComparator());
        }
        else if(sortByName.equals("Days")) {
            Collections.sort(itemListForSort, new DayComparator());
        }

        sortDataByOrder();
    }

    private void sortDataByOrder(){

        if (orderName.equals("Descending")) {
            Collections.reverse(itemListForSort);
        }

        clearData();
        setSimilarItems();
    }

    private void clearData() {
        itemIds.clear();
        itemImages.clear();
        itemTitles.clear();
        itemShippings.clear();
        itemTimeLefts.clear();
        itemPrices.clear();
        itemUrls.clear();
    }

}

class NameComparator implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
        int compare = 0;
        try
        {
            String keyA = jsonObjectA.getString("productName");
            String keyB = jsonObjectB.getString("productName");
            compare = keyA.compareTo(keyB);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return compare;
    }
}

class PriceComparator implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
        int compare = 0;
        try
        {
            Float keyA = Float.valueOf(jsonObjectA.getString("price"));
            Float keyB = Float.valueOf(jsonObjectB.getString("price"));
            compare = keyA.compareTo(keyB);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return compare;
    }
}

class DayComparator implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
        int compare = 0;
        try {
            Float keyA = Float.valueOf(jsonObjectA.getString("dayLeft"));
            Float keyB = Float.valueOf(jsonObjectB.getString("dayLeft"));
            compare = keyA.compareTo(keyB);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return compare;
    }
}

