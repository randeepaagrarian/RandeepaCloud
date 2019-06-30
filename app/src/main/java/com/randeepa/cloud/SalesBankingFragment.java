package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.randeepa.cloud.structs.CommonStruct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SalesBankingFragment extends Fragment implements View.OnClickListener {

    private Utils utils;

    private EditText date_ET;
    private EditText bank_ET;
    private EditText branch_ET;
    private EditText chassis_number_ET;
    private EditText receipt_number_ET;

    private ImageView image_IV;

    private Spinner payment_type_SP;

    private ProgressDialog paymentTypeDialog;

    private ArrayList<CommonStruct> paymentTypes;

    private AlertDialog reportSalesBankingStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View salesBanking = inflater.inflate(R.layout.fragment_sales_banking, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        date_ET = salesBanking.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        payment_type_SP = salesBanking.findViewById(R.id.payment_type_SP);
        paymentTypes = new ArrayList<>();
        loadSpinner("paymentTypes");

        return salesBanking;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_ET:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        date_ET.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                }, utils.getYear(), utils.getMonth() - 1, utils.getDay());
                datePickerDialog.show();
                break;
        }
    }

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "paymentTypes":
                url = "https://www.randeepa.cloud/android-api6/mac_bank_pay_types";
                paymentTypeDialog = new ProgressDialog(getActivity());
                paymentTypeDialog.setTitle("Loading Payment Types...");
                paymentTypeDialog.setMessage("Please wait while the dealer list is loaded.");
                paymentTypeDialog.setCancelable(false);
                paymentTypeDialog.show();
                break;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject spinnerRes = new JSONObject(response);

                    boolean validResponse = Boolean.parseBoolean(spinnerRes.getString("status"));

                    if (validResponse) {
                        JSONArray spinnerResArray = spinnerRes.getJSONArray("message");
                        for (int i = 0; i < spinnerResArray.length(); i++) {
                            JSONObject spinnerResArrayItem = spinnerResArray.getJSONObject(i);

                            switch (name) {
                                case "paymentTypes":
                                    paymentTypes.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                            }
                        }

                        switch (name) {
                            case "paymentTypes":
                                ArrayAdapter<CommonStruct> showroomDealerArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, paymentTypes);
                                payment_type_SP.setAdapter(showroomDealerArrayAdapter);
                                paymentTypeDialog.dismiss();
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                params.put("region", userDetails.getString("region", null));
                params.put("territory", userDetails.getString("territory", null));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        mQueue.add(request);
    }
}
