package com.example.hw9;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wssholmes.stark.circular_score.CircularScoreView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemShippingFragment extends Fragment {
    private static final String TAG = "ItemShippingFragment";

    private JSONArray itemShippingInfo;

    String storeName, storeURL, feedbackScore, popularity, feedbackRatingStar;
    String cost, global, time, condition;

    ConstraintLayout soldByLayout, shippingLayout, returnLayout;
    View line1, line2;

    // Sold by
    TableRow soldByRow1, soldByRow2, soldByRow3, soldByRow4;
    TextView storeNameText, feedbackScoreText;
    CircularScoreView popularityView;
    ImageView feedbackRatingStarView;

    // Shipping info
    TableRow shippingRow1, shippingRow2, shippingRow3, shippingRow4;
    TextView costContent, globalContent, timeContent, conditionContent;

    // Return policy
    TableRow returnsRow1, returnsRow2, returnsRow3, returnsRow4;
    TextView policyContent, returnsContent, refundContent, shipContent;

    boolean alertFlag = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_shipping, container, false);

        final String itemId = ((ItemActivity) getActivity()).itemId;
        final String itemShippingInfoStr = ((ItemActivity) getActivity()).shippingInfo;

        // Progress bar
        final ProgressBar progressBar = view.findViewById(R.id.tab2_progressBar);
        final TextView progressText = view.findViewById(R.id.tab2_progressText);

        // Alert
        final TextView alert = view.findViewById(R.id.tab2_alert);

        soldByLayout = view.findViewById(R.id.tab2_soldByLayout);
        shippingLayout = view.findViewById(R.id.tab2_shippingLayout);
        returnLayout = view.findViewById(R.id.tab2_returnLayout);

        line1 = view.findViewById(R.id.tab2_line1);
        line2 = view.findViewById(R.id.tab2_line2);

        // Sold By
        soldByRow1 = view.findViewById(R.id.tab2_soldByTableRow1);
        soldByRow2 = view.findViewById(R.id.tab2_soldByTableRow2);
        soldByRow3 = view.findViewById(R.id.tab2_soldByTableRow3);
        soldByRow4 = view.findViewById(R.id.tab2_soldByTableRow4);
        storeNameText = view.findViewById(R.id.tab2_storeNameContent);
        feedbackScoreText = view.findViewById(R.id.tab2_feedbackScoreContent);
        popularityView = view.findViewById(R.id.tab2_popularityContent);
        feedbackRatingStarView = view.findViewById(R.id.tab2_feedbackStarContent);

        // Shipping Info
        shippingRow1 = view.findViewById(R.id.tab2_shippingTableRow1);
        shippingRow2 = view.findViewById(R.id.tab2_shippingTableRow2);
        shippingRow3 = view.findViewById(R.id.tab2_shippingTableRow3);
        shippingRow4 = view.findViewById(R.id.tab2_shippingTableRow4);
        costContent = view.findViewById(R.id.tab2_costContent);
        globalContent = view.findViewById(R.id.tab2_globalContent);
        timeContent = view.findViewById(R.id.tab2_timeContent);
        conditionContent = view.findViewById(R.id.tab2_conditionContent);

        // Return policy
        returnsRow1 = view.findViewById(R.id.tab2_returnTableRow1);
        returnsRow2 = view.findViewById(R.id.tab2_returnTableRow2);
        returnsRow3 = view.findViewById(R.id.tab2_returnTableRow3);
        returnsRow4 = view.findViewById(R.id.tab2_returnTableRow4);
        policyContent = view.findViewById(R.id.tab2_policyContent);
        returnsContent = view.findViewById(R.id.tab2_returnsContent);
        refundContent = view.findViewById(R.id.tab2_refundContent);
        shipContent = view.findViewById(R.id.tab2_shipContent);


        RequestQueue queue = Volley.newRequestQueue(ItemShippingFragment.this.getActivity());
        String url = API.BASE_URL + "/itemDetail?itemId=" + itemId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            itemShippingInfo = new JSONArray(itemShippingInfoStr);
//                            Log.d(TAG, response);

                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);

                            int cnt = 0, cnt2 = 0, cnt3 = 0, alertCnt = 0;

                            if (jsonObject.has("ack") && jsonObject.getString("ack").equals("Success")) {
                                JSONObject itemObj = jsonObject.getJSONObject("seller");

                                // Sold by
                                storeName = itemObj.getString("storeName");
                                storeURL = itemObj.getString("storeURL");
                                feedbackScore = itemObj.getString("feedbackScore");
                                popularity = itemObj.getString("popularity");
                                feedbackRatingStar = itemObj.getString("feedbackRatingStar");

                                if (storeName.isEmpty()) {
                                    cnt++;
//                                    Log.d(TAG, "onResponse: " + cnt);
                                    soldByRow1.setVisibility(View.GONE);
                                } else {
                                    storeNameText.setPaintFlags(storeNameText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                    storeNameText.setText(storeName);

                                    if (!storeURL.isEmpty()) {
                                        storeNameText.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeURL));
                                                startActivity(browserIntent);
                                            }
                                        });
                                    }
                                }

                                if (feedbackScore.isEmpty()) {
                                    cnt++;
                                    soldByRow2.setVisibility(View.GONE);
                                } else {
                                    feedbackScoreText.setText(feedbackScore);
                                }

                                if (popularity.isEmpty()) {
                                    cnt++;
                                    soldByRow3.setVisibility(View.GONE);
                                } else {
                                    popularityView.setScore((int) Float.parseFloat(popularity));
                                }

                                if (feedbackRatingStar.isEmpty() || (int) Float.parseFloat(feedbackScore) < 10) {
                                    cnt++;
                                    soldByRow4.setVisibility(View.GONE);
                                } else {
                                    feedbackRatingStarView.setImageResource(getResourcesByScore((int) Float.parseFloat(feedbackScore)));
                                }

                                if (cnt == 4) {
                                    alertCnt++;
                                    line1.setVisibility(View.GONE);
                                    soldByLayout.setVisibility(View.GONE);
                                }

                                // Shipping Info
                                global = jsonObject.getString("globalShipping");
                                if (global.isEmpty()) {
                                    cnt2++;
                                    shippingRow2.setVisibility(View.GONE);
                                } else if (global.equals("true")) {
                                    globalContent.setText(R.string.yes);
                                } else {
                                    globalContent.setText(R.string.no);
                                }

                                condition = jsonObject.getString("conditionDescription");
                                if (condition.isEmpty()) {
                                    cnt2++;
                                    shippingRow4.setVisibility(View.GONE);
                                } else {
                                    conditionContent.setText(condition);
                                }

                                // Return Policy
                                JSONObject itemReturnObj = jsonObject.getJSONObject("returnPolicy");
                                if (!itemReturnObj.has("ReturnsAccepted")) {
                                    cnt3++;
                                    returnsRow1.setVisibility(View.GONE);
                                } else {
                                    policyContent.setText(itemReturnObj.getString("ReturnsAccepted"));
                                }

                                if (!itemReturnObj.has("ReturnsWithin")) {
                                    cnt3++;
                                    returnsRow2.setVisibility(View.GONE);
                                } else {
                                    returnsContent.setText(itemReturnObj.getString("ReturnsWithin"));
                                }

                                if (!itemReturnObj.has("Refund")) {
                                    cnt3++;
                                    returnsRow3.setVisibility(View.GONE);
                                } else {
                                    refundContent.setText(itemReturnObj.getString("Refund"));
                                }

                                if (!itemReturnObj.has("ShippingCostPaidBy")) {
                                    cnt3++;
                                    returnsRow4.setVisibility(View.GONE);
                                } else {
                                    shipContent.setText(itemReturnObj.getString("ShippingCostPaidBy"));
                                }

                                if (cnt3 == 4) {
                                    alertCnt++;
                                    line2.setVisibility(View.GONE);
                                    returnLayout.setVisibility(View.GONE);
                                }

                            } else {
                                alertCnt = 2;
                                cnt2 = 2;
                                line1.setVisibility(View.GONE);
                                soldByLayout.setVisibility(View.GONE);
                                line2.setVisibility(View.GONE);
                                returnLayout.setVisibility(View.GONE);
                            }

                            if (itemShippingInfo.length() > 0) {
                                alertFlag = false;
//                                Log.d(TAG, itemShippingInfoStr);
                                JSONObject itemShipping = itemShippingInfo.getJSONObject(0);

                                // Shipping Info
                                cost = itemShipping.getString("shippingCost");
                                if (cost.equals("N/A")) {
                                    cnt2++;
                                    shippingRow1.setVisibility(View.GONE);
                                } else {
                                    costContent.setText(cost);
                                }

                                if (itemShipping.has("handlingTime")) {
                                    time = itemShipping.getJSONArray("handlingTime").getString(0);

                                    if (time.equals("0") || time.equals("1")) {
                                        timeContent.setText(String.format("%s Day", time));
                                    } else {
                                        timeContent.setText(String.format("%s Days", time));
                                    }
                                } else {
                                    cnt2++;
                                    shippingRow4.setVisibility(View.GONE);
                                }

                                if (cnt2 == 4) {
                                    alertCnt++;
                                    line1.setVisibility(View.GONE);
                                    shippingLayout.setVisibility(View.GONE);
                                }
                            } else {
                                alertCnt++;
                                line1.setVisibility(View.GONE);
                                shippingLayout.setVisibility(View.GONE);
                            }

                            if (alertCnt == 3) {
                                alert.setVisibility(View.VISIBLE);
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
                Log.d(TAG, "Error in tab1");
            }
        });

        queue.add(stringRequest);


        return view;
    }

    private int getResourcesByScore(int score) {
        if (score < 50) {
            return R.drawable.star_circle_outline_yellow;
        } else if (score < 100) {
            return R.drawable.star_circle_outline_blue;
        } else if (score < 500) {
            return R.drawable.star_circle_outline_turquoise;
        } else if (score < 1000) {
            return R.drawable.star_circle_outline_purple;
        } else if (score < 5000) {
            return R.drawable.star_circle_outline_red;
        } else if (score < 10000) {
            return R.drawable.star_circle_outline_green;
        } else if (score < 25000) {
            return R.drawable.star_circle_yellow;
        } else if (score < 50000) {
            return R.drawable.star_circle_turquoise;
        } else if (score < 100000) {
            return R.drawable.star_circle_purple;
        } else if (score < 500000) {
            return R.drawable.star_circle_red;
        } else if (score < 1000000) {
            return R.drawable.star_circle_green;
        } else {
            return R.drawable.star_circle_silver;
        }

    }
}
