package com.example.hw9;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class ItemPhotoFragment extends Fragment {
    private static final String TAG = "ItemPhotoFragment";

    JSONArray image;

    LinearLayout photo;
    LayoutInflater photoInflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_photo, container, false);

        final String title = ((ItemActivity) getActivity()).title;

        // Progress bar
        final ProgressBar progressBar = view.findViewById(R.id.tab3_progressBar);
        final TextView progressText = view.findViewById(R.id.tab3_progressText);

        // Alert
        final TextView alert = view.findViewById(R.id.tab3_alert);

        photo = view.findViewById(R.id.itemPhotos);
        photoInflater = LayoutInflater.from(this.getActivity());

        RequestQueue queue = Volley.newRequestQueue(ItemPhotoFragment.this.getActivity());
        String url = API.BASE_URL + "/photoDetail?title=" + title;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                            response = "{\"size\":8,\"image\":[{\"link\":\"https://pics.four13.co/AU200s/AU230a.jpg\"},{\"link\":\"https://www.picclickimg.com/d/w1600/pict/152833605442_/USC-Trojans-Nike-Fit-Dry-Pants-L-Gray.jpg\"},{\"link\":\"https://pics.four13.co/AU200s/AU231f.jpg\"},{\"link\":\"https://i.pinimg.com/originals/4b/78/78/4b78782a235a9e722384f1458dcc4e97.jpg\"},{\"link\":\"https://pics.four13.co/AU200s/AU230f.jpg\"},{\"link\":\"https://i.ebayimg.com/images/g/4UwAAOSwNgNbg-6O/s-l1600.jpg\"},{\"link\":\"https://pics.four13.co/AF100s/AF164a.jpg\"},{\"link\":\"https://www.teammerchandise.net/wp-content/uploads/2019/01/57-164.jpg\"}],\"image0\":[{\"link\":\"https://pics.four13.co/AU200s/AU230a.jpg\"},{\"link\":\"https://i.pinimg.com/originals/4b/78/78/4b78782a235a9e722384f1458dcc4e97.jpg\"}],\"image1\":[{\"link\":\"https://www.picclickimg.com/d/w1600/pict/152833605442_/USC-Trojans-Nike-Fit-Dry-Pants-L-Gray.jpg\"},{\"link\":\"https://pics.four13.co/AU200s/AU230f.jpg\"},{\"link\":\"https://pics.four13.co/AF100s/AF164a.jpg\"}],\"image2\":[{\"link\":\"https://pics.four13.co/AU200s/AU231f.jpg\"},{\"link\":\"https://i.ebayimg.com/images/g/4UwAAOSwNgNbg-6O/s-l1600.jpg\"},{\"link\":\"https://www.teammerchandise.net/wp-content/uploads/2019/01/57-164.jpg\"}]}";

                            JSONObject jsonObject = new JSONObject(response);
//                            Log.d(TAG, response);

                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);

                            if (jsonObject.has("size") && Integer.valueOf(jsonObject.get("size").toString()) != 0) {
                                image = jsonObject.getJSONArray("image");

                                for (int i = 0; i < image.length(); i++) {
                                    View v = photoInflater.inflate(R.layout.tab3_image, photo, false);

                                    ImageView imageView = v.findViewById(R.id.tab3_imageView);
                                    Glide.with(ItemPhotoFragment.this.getActivity())
                                            .asBitmap()
                                            .load(image.getJSONObject(i).getString("link"))
                                            .into(imageView);
                                    photo.addView(v);
                                }

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
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(stringRequest);

        return view;
    }
}
