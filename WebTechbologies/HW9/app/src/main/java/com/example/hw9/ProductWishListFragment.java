package com.example.hw9;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class ProductWishListFragment extends Fragment {

    private static final String TAG = "ProductWishListFragment";

    View view;

    private ArrayList<String> itemIds = new ArrayList<>();
    private ArrayList<String> itemImages = new ArrayList<>();
    private ArrayList<String> itemTitles = new ArrayList<>();
    private ArrayList<String> itemShippings = new ArrayList<>();
    private ArrayList<String> itemZips = new ArrayList<>();
    private ArrayList<String> itemConditions = new ArrayList<>();
    private ArrayList<String> itemPrices = new ArrayList<>();
    private ArrayList<String> itemCarts = new ArrayList<>();
    private ArrayList<JSONArray> itemShippingInfos = new ArrayList<>();

    private ArrayList<String> itemDetails = new ArrayList<>();

    TextView alert;
    TextView cnt;
    TextView price;

    SharedPreferences wishList;
    SharedPreferences.Editor wishListEditor;

    GridLayoutManager gridLayoutManager;
    RecyclerView recyclerView;
    ProductWishlistItemRecyclerAdapter recyclerAdapter;
    Parcelable recyclerViewState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.product_wishlist, container, false);

        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView = view.findViewById(R.id.wishListRecycler);
        recyclerAdapter = new ProductWishlistItemRecyclerAdapter(getContext());
        recyclerView.setLayoutManager(gridLayoutManager);


        alert = view.findViewById(R.id.wishList_alert);
        cnt = view.findViewById(R.id.wishList_num);
        price = view.findViewById(R.id.wishList_price);

        wishList = PreferenceManager.getDefaultSharedPreferences(getContext());
        wishListEditor = wishList.edit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        clearData();
        setItemDetail();
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

        setAlert();
        setText();

    }

    @Override
    public void onPause() {
        super.onPause();

        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void setAlert() {
        if (itemDetails.size() == 0) {
            alert.setVisibility(View.VISIBLE);
        } else {
            alert.setVisibility(View.GONE);
        }
    }

    private void setText() {
        cnt.setText(String.format("%s", itemDetails.size()));
        Float totalPrice = 0.0f;
        for (int i = 0; i < itemPrices.size(); i++) {
            String priceStr = itemPrices.get(i);
            if (!priceStr.isEmpty()) {
                totalPrice += Float.valueOf(priceStr.substring(priceStr.indexOf("$") + 1));
            }
        }
        price.setText(String.format("$%.2f", totalPrice));
    }

    private void clearData() {
        itemDetails = new ArrayList<>();
        itemIds = new ArrayList<>();
        itemImages = new ArrayList<>();
        itemTitles = new ArrayList<>();
        itemShippings = new ArrayList<>();
        itemZips = new ArrayList<>();
        itemConditions = new ArrayList<>();
        itemPrices = new ArrayList<>();
        itemCarts = new ArrayList<>();
        itemShippingInfos = new ArrayList<>();
    }

    private void setItemDetail() {
        try {
            Map<String, ?> allEntries = wishList.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                itemDetails.add(entry.getValue().toString());
            }

            for (int i = 0; i < itemDetails.size(); i++) {
                JSONObject jsonObj = new JSONObject(itemDetails.get(i));
                String itemId, itemImage, itemCart, itemTitle, itemZip, itemShipping, itemCondition, itemPrice;
                JSONArray itemShippingInfo;

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

            }

            setRecyclerView();

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private void setRecyclerView() {
        recyclerView.setAdapter(null);
        recyclerAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(recyclerAdapter);
    }

    public class ProductWishlistItemRecyclerAdapter extends RecyclerView.Adapter<ProductWishlistItemRecyclerAdapter.ViewHolder> {
        private static final String TAG = "ProductWishlistItemRecyclerAdapter";

        private Context context;

        public ProductWishlistItemRecyclerAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);

            wishList = PreferenceManager.getDefaultSharedPreferences(context);
            wishListEditor = wishList.edit();

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            Glide.with(context)
                    .asBitmap()
                    .load(itemImages.get(position))
                    .into(holder.itemImage);

            String itemTitleShort = itemTitles.get(position);
            if (itemTitleShort.length() > 50) {
                itemTitleShort = String.format("%s...", itemTitleShort.substring(0, 50));
            }
            holder.itemTitle.setText(itemTitleShort);
//            holder.itemTitle.setText(itemTitles.get(position));

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
                    Toast.makeText(context, itemTitles.get(position) + " was removed from wish list", Toast.LENGTH_LONG).show();

                    wishListEditor.remove(itemIds.get(position));
                    wishListEditor.commit();

                    holder.itemCart.setImageResource(R.drawable.cart_plus);

                    clearData();
                    setItemDetail();
                    setAlert();
                    setText();

                    notifyItemRemoved(position);
                    notifyDataSetChanged();

                }
            });
        }

        @Override
        public int getItemCount() {
            return itemDetails.size();
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
