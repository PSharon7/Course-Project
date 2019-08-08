package com.example.hw9;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductResults extends AppCompatActivity {
    private static final String TAG = "ProductResults";

    private String url;
    private String keyword;

    private ArrayList<String> itemIds = new ArrayList<>();
    private ArrayList<String> itemImages = new ArrayList<>();
    private ArrayList<String> itemTitles = new ArrayList<>();
    private ArrayList<String> itemShippings = new ArrayList<>();
    private ArrayList<String> itemZips = new ArrayList<>();
    private ArrayList<String> itemConditions = new ArrayList<>();
    private ArrayList<String> itemPrices = new ArrayList<>();
    private ArrayList<String> itemCarts = new ArrayList<>();
    private ArrayList<JSONArray> itemShippingInfos = new ArrayList<>();


    // FOR WISH LIST
    private ArrayList<String> itemDetails = new ArrayList<>();

    private SharedPreferences wishList;
    private SharedPreferences.Editor wishListEditor;

    private Integer itemSize;
    private JSONArray itemArray;

    private ConstraintLayout resultText;
    private TextView count;
    private TextView keywordText;

    GridLayoutManager gridLayoutManager;
    RecyclerView recyclerView;
    ProductResultsItemRecyclerAdapter recyclerAdapter;
    Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_results);

        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView = findViewById(R.id.resultRecycler);
        recyclerAdapter = new ProductResultsItemRecyclerAdapter(this);
        recyclerView.setLayoutManager(gridLayoutManager);

        Toolbar backToolbar = findViewById(R.id.result_back);
        setSupportActionBar(backToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // progress bar
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView progressText = findViewById(R.id.progressText);

        // Alert
        final TextView alert = findViewById(R.id.alert);

        resultText = findViewById(R.id.resultText);
        count = findViewById(R.id.resultText2);
        keywordText = findViewById(R.id.resultText4);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        keyword = intent.getStringExtra("keyword");
        Log.d("url", url);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
//                            Log.d(TAG, response);

                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);


                            if (jsonObject.has("ack") && jsonObject.get("ack").toString().equals("Success")
                                    && Integer.valueOf(jsonObject.get("itemSize").toString()) > 0) {

                                resultText.setVisibility(View.VISIBLE);
                                itemSize = Integer.valueOf(jsonObject.get("itemSize").toString());
                                itemArray = jsonObject.getJSONArray("searchResult");

                                count.setText(itemSize.toString());
                                keywordText.setText(keyword);
                                setItemResult();

                            } else {
                                // Fail
                                alert.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            //ERROR
                            alert.setVisibility(View.VISIBLE);
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ERROR
                Log.e(TAG, error.toString());
            }
        });

        queue.add(stringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();

        setRecyclerView();

        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    @Override
    public void onPause() {
        super.onPause();

        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    private void setItemResult() {
        try {
            for (int i = 1; i < itemArray.length(); i++) {
                JSONArray jsonArr = itemArray.getJSONArray(i);
                for (int j = 0; j < jsonArr.length(); j++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(j);
                    String itemId, itemImage, itemCart, itemTitle, itemZip, itemShipping, itemCondition, itemPrice;
                    JSONArray itemShippingInfo;

                    String itemDetail = jsonObj.toString();

                    itemId = jsonObj.getString("itemId");
                    itemImage = jsonObj.getString("image");
                    itemTitle = jsonObj.getString("title");
                    itemZip = jsonObj.getString("zip");
                    itemShipping = jsonObj.getString("shipping");
                    itemPrice = jsonObj.getString("price");
                    itemShippingInfo = jsonObj.getJSONArray("shippingInfo");
                    itemCondition = jsonObj.getString("condition");

                    itemCart = "";

                    itemIds.add(itemId);
                    itemImages.add(itemImage);
                    itemTitles.add(itemTitle);
                    itemZips.add(itemZip);
                    itemShippings.add(itemShipping);
                    itemPrices.add(itemPrice);
                    itemConditions.add(itemCondition);
                    itemCarts.add(itemCart);
                    itemShippingInfos.add(itemShippingInfo);

                    itemDetails.add(itemDetail);
                }
            }

            setRecyclerView();
        } catch (JSONException e) {
            //ERROR
            Log.d(TAG, "Error in setItemResult");
        }
    }

    private void setRecyclerView() {
        recyclerView.setAdapter(null);
        recyclerAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerAdapter);
    }

    //Return btn in the tool bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public class ProductResultsItemRecyclerAdapter extends RecyclerView.Adapter<ProductResultsItemRecyclerAdapter.ViewHolder> {
        private static final String TAG = "ProductResultsItemRecyclerAdapter";

        private Context context;


        public ProductResultsItemRecyclerAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ProductResultsItemRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
            ProductResultsItemRecyclerAdapter.ViewHolder viewHolder = new ProductResultsItemRecyclerAdapter.ViewHolder(v);

            wishList = PreferenceManager.getDefaultSharedPreferences(context);
            wishListEditor = wishList.edit();

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ProductResultsItemRecyclerAdapter.ViewHolder holder, final int position) {
            Glide.with(context)
                    .asBitmap()
                    .load(itemImages.get(position))
                    .into(holder.itemImage);

            String itemTitleShort = itemTitles.get(position);
            if (itemTitleShort.length() > 50) {
                itemTitleShort = String.format("%s...", itemTitleShort.substring(0, 50));
            }
            holder.itemTitle.setText(itemTitleShort);

            holder.itemZip.setText(String.format("Zip: %s", itemZips.get(position)));
            holder.itemShipping.setText(itemShippings.get(position));
            holder.itemPrice.setText(itemPrices.get(position));
            holder.itemCondition.setText(itemConditions.get(position));

            if (wishList.contains(itemIds.get(position))) {
                holder.itemCart.setImageResource(R.drawable.cart_remove);
            } else {
                holder.itemCart.setImageResource(R.drawable.cart_plus);
            }

            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, ItemActivity.class);
                    intent.putExtra("itemId", itemIds.get(position));
                    intent.putExtra("itemTitle", itemTitles.get(position));
                    intent.putExtra("itemShipping", itemShippings.get(position));
                    intent.putExtra("itemShippingInfo", itemShippingInfos.get(position).toString());
                    intent.putExtra("itemDetail", itemDetails.get(position));

                    context.startActivity(intent);

                }
            });

            holder.itemCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If not in wish list
                    if (!wishList.contains(itemIds.get(position))) {
                        //add
                        Toast.makeText(context, itemTitles.get(position) + " was added to wish list", Toast.LENGTH_LONG).show();

                        wishListEditor.putString(itemIds.get(position), itemDetails.get(position));
                        wishListEditor.commit();

                        holder.itemCart.setImageResource(R.drawable.cart_remove);
                    } else {
                        //remove

                        Toast.makeText(context, itemTitles.get(position) + " was removed from wish list", Toast.LENGTH_LONG).show();

                        wishListEditor.remove(itemIds.get(position));
                        wishListEditor.commit();

                        holder.itemCart.setImageResource(R.drawable.cart_plus);

                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return itemIds.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // init the item view's
            RelativeLayout itemLayout;

            ImageView itemImage;
            ImageView itemCart;
            TextView itemTitle;
            TextView itemZip;
            TextView itemShipping;
            TextView itemCondition;
            TextView itemPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                // get the reference of item view's
                itemLayout = itemView.findViewById(R.id.itemLayout);

                itemImage = itemView.findViewById(R.id.itemImage);
                itemCart = itemView.findViewById(R.id.itemCart);
                itemTitle = itemView.findViewById(R.id.itemTitle);
                itemZip = itemView.findViewById(R.id.itemZip);
                itemShipping = itemView.findViewById(R.id.itemShipping);
                itemCondition = itemView.findViewById(R.id.itemCondition);
                itemPrice = itemView.findViewById(R.id.itemPrice);
            }
        }
    }

}
