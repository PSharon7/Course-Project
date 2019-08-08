package com.example.hw9;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductSearchFragment extends Fragment {
    private static final String TAG = "ProductSearchFragment";

    private String url;

    private String keyword;
    private EditText inputKeyword;

    private String category = "All";
    private Map<String, String> cate2id = new HashMap<String, String>() {{
        put("All", "-1");
        put("Art", "550");
        put("Baby", "2984");
        put("Books", "267");
        put("Clothing, Shoes & Accessories", "11450");
        put("Computers/Tablets & Networking", "58058");
        put("Health & Beauty", "26395");
        put("Music", "11233");
        put("Video Games & Consoles", "1249");
    }};

    private CheckBox c_new, c_used, c_unspecified;
    private CheckBox s_pickup, s_free;

    private CheckBox        enable_nearby;
    private ConstraintLayout nearbyLayout;
    private String distance = "10";
    private EditText inputDistance;
    //private RadioGroup fromGroup;
    private RadioButton currentBtn, zipBtn;
    private String zipcode, hereZipcode, inputZipcodeStr;
    private ArrayList<String> zipcodeAuto = new ArrayList<>();
    private AutoCompleteTextView inputZipcode;

    private TextView keywordWarning;
    private TextView zipWarning;

    private Button searchBtn;
    private Button clearBtn;

    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_search, container, false);

        //get lat, lng
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreateView: PERMISSION_NOT_GRANTED");
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if (addresses.size() > 0) {
                    Address returnedAddress = addresses.get(0);
                    hereZipcode = returnedAddress.getPostalCode();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        inputKeyword = view.findViewById(R.id.keyword);

        c_new = view.findViewById(R.id.c_new);
        c_used = view.findViewById(R.id.c_used);
        c_unspecified = view.findViewById(R.id.c_unspecified);

        s_pickup = view.findViewById(R.id.s_pick);
        s_free = view.findViewById(R.id.s_free);

        enable_nearby = view.findViewById(R.id.enable_nearby_check);
        nearbyLayout = view.findViewById(R.id.nearbyLayout);

        inputDistance = view.findViewById(R.id.distance);

        //fromGroup = view.findViewById(R.id.from);
        currentBtn = view.findViewById(R.id.currentBtn);
        zipBtn = view.findViewById(R.id.zipBtn);
        inputZipcode = view.findViewById(R.id.zipcode);
        inputZipcode.setEnabled(false);

        //fromGroup.check(currentBtn.getId());

        keywordWarning = view.findViewById(R.id.warning1);
        zipWarning = view.findViewById(R.id.warning2);

        searchBtn = view.findViewById(R.id.search);
        clearBtn = view.findViewById(R.id.clear);

        final Spinner cate_spinner = view.findViewById(R.id.category);
        cate_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] category_arr = getResources().getStringArray(R.array.category_arr);
                category = category_arr[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        enable_nearby.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nearbyLayout.setVisibility(View.VISIBLE);
                } else {
                    nearbyLayout.setVisibility(View.GONE);
                }
            }
        });

        currentBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inputZipcode.setEnabled(false);
                } else {
                    inputZipcode.setEnabled(true);
                }
            }
        });



        inputZipcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String input = inputZipcode.getText().toString();


                RequestQueue queue = Volley.newRequestQueue(getActivity());
                String url = API.BASE_URL + "/zipAutoComplete?zip=" + input;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray auto_zipcode = new JSONArray(response);

                            zipcodeAuto.clear();
                            for (int i = 0; i < auto_zipcode.length(); i++) {
                                zipcodeAuto.add(auto_zipcode.get(i).toString());
                            }
                            Log.d(TAG, "onResponse: " + zipcodeAuto);
                            setAdapter();

                        } catch (JSONException e) {
                            Log.d(TAG, "onResponse: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(stringRequest);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = inputKeyword.getText().toString();
                distance = inputDistance.getText().toString();
                if (!(distance.trim().length() != 0 && distance.matches("\\d+"))) {
                    distance = "10";
                }

                inputZipcodeStr = inputZipcode.getText().toString();

                if (keyword.trim().length() == 0) {
                    keywordWarning.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_LONG).show();
                } else {
                    keywordWarning.setVisibility(View.GONE);
                }

                if (enable_nearby.isChecked() && zipBtn.isChecked()) {
                    if (!(inputZipcodeStr.length() == 5 && inputZipcodeStr.matches("-?\\d+(\\.\\d+)?"))) {
                        zipWarning.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_LONG).show();
                    } else {
                        zipcode = inputZipcodeStr;
                        zipWarning.setVisibility(View.GONE);
                    }
                } else {
                    zipcode = hereZipcode;
                    zipWarning.setVisibility(View.GONE);
                }

                if (keywordWarning.getVisibility() == View.GONE && zipWarning.getVisibility() == View.GONE) {
                    url = API.BASE_URL + "/searchProduct?" +
                            "keyword=" + keyword +
                            "&category=" + cate2id.get(category) +
                            "&new=" + c_new.isChecked() + "&used=" + c_used.isChecked() + "&unspecified=" + c_unspecified.isChecked() +
                            "&pickup=" + s_pickup.isChecked() + "&shipping=" + s_free.isChecked() +
                            "&distance=" + distance +
                            "&zipcode=" + zipcode;

                    Log.d(TAG, "onClick: " + zipcode);
                    search();
                } else {
                    Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_LONG).show();
                }

            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputKeyword.setText("");
                cate_spinner.setSelection(0);
                c_new.setChecked(false);
                c_used.setChecked(false);
                c_unspecified.setChecked(false);
                s_free.setChecked(false);
                s_pickup.setChecked(false);
                enable_nearby.setChecked(false);
                inputDistance.setText("");
                currentBtn.setChecked(true);
                zipBtn.setChecked(false);
                inputZipcode.setText("");
                inputZipcode.setEnabled(false);
                inputDistance.setText("");
                keywordWarning.setVisibility(View.GONE);
                zipWarning.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public void search() {
        Intent intent = new Intent(getActivity(), ProductResults.class);
        intent.putExtra("url", url);
        intent.putExtra("keyword", keyword);
        startActivity(intent);
    }

    public void setAdapter() {
        inputZipcode.setAdapter(null);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, zipcodeAuto) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(14);
//                ((TextView) v).setHeight(20);
                ((TextView) v).setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                return v;
            }
        };

//        Log.d(TAG, "setAdapter: " + inputZipcode.getDropDownVerticalOffset());
//        Log.d(TAG, "setAdapter: " + inputZipcode.getDropDownHeight());
//        offset = inputZipcode.getDropDownVerticalOffset() - 40;
//        inputZipcode.setDropDownVerticalOffset(-200);

        inputZipcode.setThreshold(1);
        inputZipcode.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
