package com.example.hw9;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ItemSimilarRecyclerAdapter extends RecyclerView.Adapter<ItemSimilarRecyclerAdapter.ViewHolder> {
    private static final String TAG = "ItemSimilarRecyclerAdapter";

    private Context context;
    private ArrayList<String> itemIds = new ArrayList<>();
    private ArrayList<String> itemImages = new ArrayList<>();
    private ArrayList<String> itemTitles = new ArrayList<>();
    private ArrayList<Float> itemShippings = new ArrayList<>();
    private ArrayList<Float> itemTimeLefts = new ArrayList<>();
    private ArrayList<Float> itemPrices = new ArrayList<>();
    private ArrayList<String> itemUrls = new ArrayList<>();

    public ItemSimilarRecyclerAdapter(Context context,
                                      ArrayList<String> itemIds,
                                      ArrayList<String> itemImages,
                                      ArrayList<String> itemTitles,
                                      ArrayList<Float> itemShippings,
                                      ArrayList<Float> itemTimeLefts,
                                      ArrayList<Float> itemPrices,
                                      ArrayList<String> itemUrls) {
        this.context = context;
        this.itemIds = itemIds;
        this.itemImages = itemImages;
        this.itemTitles = itemTitles;
        this.itemShippings = itemShippings;
        this.itemTimeLefts = itemTimeLefts;
        this.itemPrices = itemPrices;
        this.itemUrls = itemUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab4_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Glide.with(context)
                .asBitmap()
                .load(itemImages.get(position))
                .into(holder.itemImage);

        holder.itemTitle.setText(itemTitles.get(position));

        Float numberValue;
        String textValue;

        numberValue = Float.valueOf(itemShippings.get(position));
        if (numberValue == 0) {
            textValue = "Free Shipping";
        } else {
            textValue = String.format("$%.2f", numberValue);
        }
        holder.itemShipping.setText(textValue);

        numberValue = Float.valueOf(itemTimeLefts.get(position));
        if (numberValue == 0) {
            textValue = "0";
        } else if (numberValue == 1) {
            textValue = "1 Day";
        } else {
            textValue = String.format("%.0f Days", numberValue);
        }
        holder.itemTimeLeft.setText(textValue);

        textValue = String.format("$%.2f", Float.valueOf(itemPrices.get(position)));
        holder.itemPrice.setText(textValue);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = itemUrls.get(position);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
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
        TextView itemTitle;
        TextView itemShipping;
        TextView itemTimeLeft;
        TextView itemPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            itemLayout = itemView.findViewById(R.id.tab4_itemLayout);

            itemImage = itemView.findViewById(R.id.tab4_itemImage);
            itemTitle = itemView.findViewById(R.id.tab4_itemTitle);
            itemShipping = itemView.findViewById(R.id.tab4_itemShipping);
            itemTimeLeft = itemView.findViewById(R.id.tab4_timeLeft);
            itemPrice = itemView.findViewById(R.id.tab4_itemPrice);
        }
    }
}


