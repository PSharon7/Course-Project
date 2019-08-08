package com.example.hw9;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
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


public class ItemProductFragment extends Fragment {
    private static final String TAG = "ItemProductFragment";

    JSONArray image;
    String title;
    String subtitle;
    String price;
    String brand;
    JSONArray itemSpecified;

    HorizontalScrollView imageGallery;
    LinearLayout gallery;
    LayoutInflater galleryInflater;

    TextView tab1_title, tab1_price, tab1_shipping;
    TextView tab1_subtitle, tab1_price2, tab1_brand, tab1_brand2;
    LinearLayout list;
    LayoutInflater listInflater;

    View line1, line2;
    ConstraintLayout constraintLayout1, constraintLayout2;
    TableRow field1_Row1, field1_Row2, field1_Row3;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_product, container, false);

        final String itemId = ((ItemActivity) getActivity()).itemId;
        final String itemShipping = ((ItemActivity) getActivity()).itemShipping;

        // Progress bar
        final ProgressBar progressBar = view.findViewById(R.id.tab1_progressBar);
        final TextView progressText = view.findViewById(R.id.tab1_progressText);

        // Alert
        final TextView alert = view.findViewById(R.id.tab1_alert);

        imageGallery = view.findViewById(R.id.imageGalleryView);
        gallery = view.findViewById(R.id.imageGallery);
        galleryInflater = LayoutInflater.from(this.getActivity());

        tab1_title = view.findViewById(R.id.tab1_title);
        tab1_price = view.findViewById(R.id.tab1_priceInTitle);
        tab1_shipping = view.findViewById(R.id.tab1_shipping);

        tab1_subtitle = view.findViewById(R.id.tab1_subtitleContent);
        tab1_price2 = view.findViewById(R.id.tab1_priceContent);
        tab1_brand = view.findViewById(R.id.tab1_brandContent);
        tab1_brand2 = view.findViewById(R.id.tab1_brand2Content);

        list = view.findViewById(R.id.itemSpecifiedList);
        listInflater = LayoutInflater.from(this.getActivity());

        line1 = view.findViewById(R.id.tab1_line1);
        constraintLayout1 = view.findViewById(R.id.tab1_field1);
        field1_Row1 = view.findViewById(R.id.tab1_highlightTableRow1);
        field1_Row2 = view.findViewById(R.id.tab1_highlightTableRow2);
        field1_Row3 = view.findViewById(R.id.tab1_highlightTableRow3);

        line2 = view.findViewById(R.id.tab1_line2);
        constraintLayout2 = view.findViewById(R.id.tab1_field2);


        RequestQueue queue = Volley.newRequestQueue(ItemProductFragment.this.getActivity());
        String url = API.BASE_URL + "/itemDetail?itemId=" + itemId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
//                            Log.d(TAG, "onResponse: " + response);

                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);

                            if (jsonObject.has("ack") && jsonObject.getString("ack").equals("Success")) {
                                JSONObject itemObj = jsonObject.getJSONObject("item");

                                // Get data
                                image = itemObj.getJSONArray("image");
                                title = itemObj.getString("title");
                                price = itemObj.getString("price");

                                int cnt = 0;
                                subtitle = itemObj.getString("subTitle");
                                if (subtitle.isEmpty()) {
                                    field1_Row1.setVisibility(View.GONE);
                                    cnt++;
                                }

                                if (price.isEmpty()) {
                                    field1_Row2.setVisibility(View.GONE);
                                    cnt++;
                                }

                                brand = itemObj.getString("brand");
                                if (brand.isEmpty()) {
                                    field1_Row3.setVisibility(View.GONE);
                                    cnt++;
                                }

                                if (cnt == 3) {
                                    line1.setVisibility(View.GONE);
                                    constraintLayout1.setVisibility(View.GONE);
                                }

                                itemSpecified = itemObj.getJSONArray("itemSpecifics");

                                // Set data
                                for (int i = 0; i < image.length(); i++) {
                                    View v = galleryInflater.inflate(R.layout.tab1_image, gallery, false);

                                    ImageView imageView = v.findViewById(R.id.tab3_imageView);
                                    Glide.with(ItemProductFragment.this.getActivity())
                                            .asBitmap()
                                            .load(image.get(i))
                                            .into(imageView);
                                    gallery.addView(v);
                                }
                                if (image.length() == 0) {
                                    imageGallery.setVisibility(View.GONE);
                                }

                                tab1_title.setText(title);
                                tab1_price.setText(price);
                                String shippingStr = "";
                                if (itemShipping.equals("Free Shipping")) {
                                    shippingStr = "With Free Shipping";
                                } else if (itemShipping.equals("N/A")) {
                                    shippingStr = "";
                                } else {
                                    shippingStr = String.format("With %s Shipping", itemShipping);
                                }
                                tab1_shipping.setText(shippingStr);

                                tab1_subtitle.setText(subtitle);
                                tab1_price2.setText(price);
                                tab1_brand.setText(brand);

                                if (brand.isEmpty()) {
                                    tab1_brand2.setVisibility(View.GONE);
                                } else {
                                    tab1_brand2.setText("• " + brand);
                                }

                                for (int i = 0; i < itemSpecified.length(); i++) {
                                    View v = listInflater.inflate(R.layout.tab1_specified, list, false);

                                    TextView textView = v.findViewById(R.id.item_specified);
                                    textView.setText("• " + itemSpecified.getJSONObject(i).getString("value"));
                                    list.addView(v);
                                }

                                if (itemSpecified.length() == 0 && brand.isEmpty()) {
                                    line2.setVisibility(View.GONE);
                                    constraintLayout2.setVisibility(View.GONE);
                                }

                            } else {
                                // Fail
                                alert.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            //ERROR
                            alert.setVisibility(View.VISIBLE);
                            Log.d("JSONError : ", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //ERROR
                Log.d("Error", "Error in tab1");
            }
        });

        queue.add(stringRequest);


        return view;
    }
}
