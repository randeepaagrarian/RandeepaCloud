package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

public class ReportSaleFragment extends Fragment implements View.OnClickListener {

    private Utils utils;

    private EditText date_ET;
    private EditText chassis_number_ET;
    private EditText price_ET;
    private EditText invoice_number_ET;
    private EditText customer_name_ET;
    private EditText customer_contact_number_ET;
    private EditText customer_address_ET;
    private EditText institute_ET;
    private EditText advance_ET;

    private Spinner showroom_dealer_SP;
    private Spinner model_SP;
    private Spinner sale_type_SP;

    private Button report_sale_BT;

    private ProgressDialog showroomDealerListDialog;
    private ProgressDialog modelListDialog;
    private ProgressDialog saleTypeDialog;
    private ProgressDialog reportingSaleDialog;

    private ArrayList<CommonStruct> showroomsDealers;
    private ArrayList<CommonStruct> models;
    private ArrayList<CommonStruct> saleTypes;

    private AlertDialog reportSaleStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View reportSale = inflater.inflate(R.layout.fragment_report_sale, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        validateAPIKey();

        date_ET = reportSale.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        showroom_dealer_SP = reportSale.findViewById(R.id.showroom_dealer_SP);
        showroomsDealers = new ArrayList<>();
        loadSpinner("showroomDealer");

        model_SP = reportSale.findViewById(R.id.model_SP);
        models = new ArrayList<>();
        loadSpinner("model");

        sale_type_SP = reportSale.findViewById(R.id.sale_type_SP);
        saleTypes = new ArrayList<>();
        loadSpinner("saleTypes");

        chassis_number_ET = reportSale.findViewById(R.id.chassis_number_ET);
        price_ET = reportSale.findViewById(R.id.price_ET);
        invoice_number_ET = reportSale.findViewById(R.id.invoice_number_ET);
        customer_name_ET = reportSale.findViewById(R.id.customer_name_ET);
        customer_contact_number_ET = reportSale.findViewById(R.id.customer_contact_number_ET);
        customer_address_ET = reportSale.findViewById(R.id.customer_address_ET);
        institute_ET = reportSale.findViewById(R.id.institute_ET);
        advance_ET = reportSale.findViewById(R.id.advance_ET);

        report_sale_BT = reportSale.findViewById(R.id.report_sale_BT);
        report_sale_BT.setOnClickListener(this);


        return reportSale;
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
            case R.id.report_sale_BT:

                if (TextUtils.isEmpty(date_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(chassis_number_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the chassis number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(price_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the price", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(customer_name_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the customer name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(customer_contact_number_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the customer contact number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(customer_address_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the customer address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(advance_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the advance", Toast.LENGTH_SHORT).show();
                    return;
                }

                reportSale();

                break;
        }
    }

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "showroomDealer":
                url = "https://www.randeepa.cloud/android-api6/get_dealers";
                showroomDealerListDialog = new ProgressDialog(getActivity());
                showroomDealerListDialog.setTitle("Loading Dealer List...");
                showroomDealerListDialog.setMessage("Please wait while the dealer list is loaded.");
                showroomDealerListDialog.setCancelable(false);
                showroomDealerListDialog.show();
                break;
            case "model":
                modelListDialog = new ProgressDialog(getActivity());
                modelListDialog.setTitle("Loading Model List...");
                modelListDialog.setMessage("Please wait while the model list is loaded.");
                modelListDialog.setCancelable(false);
                modelListDialog.show();
                url = "https://www.randeepa.cloud/android-api6/sale_report_models";
                break;
            case "saleTypes":
                saleTypeDialog = new ProgressDialog(getActivity());
                saleTypeDialog.setTitle("Loading Sale Types...");
                saleTypeDialog.setMessage("Please wait while the sales types are loaded.");
                saleTypeDialog.setCancelable(false);
                saleTypeDialog.show();
                url = "https://www.randeepa.cloud/android-api6/sale_report_sale_type";
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
                                case "showroomDealer":
                                    showroomsDealers.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                                case "model":
                                    models.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                                case "saleTypes":
                                    saleTypes.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                            }
                        }

                        switch (name) {
                            case "showroomDealer":
                                ArrayAdapter<CommonStruct> showroomDealerArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, showroomsDealers);
                                showroom_dealer_SP.setAdapter(showroomDealerArrayAdapter);
                                showroomDealerListDialog.dismiss();
                                break;
                            case "model":
                                ArrayAdapter<CommonStruct> modelArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, models);
                                model_SP.setAdapter(modelArrayAdapter);
                                modelListDialog.dismiss();
                                break;
                            case "saleTypes":
                                ArrayAdapter<CommonStruct> salesTypeArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, saleTypes);
                                sale_type_SP.setAdapter(salesTypeArrayAdapter);
                                saleTypeDialog.dismiss();
                                break;
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                signOut();
                Toast.makeText(getActivity(), "Cannot reach cloud servers.", Toast.LENGTH_SHORT).show();
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

    private void reportSale() {

        reportingSaleDialog = new ProgressDialog(getActivity());
        reportingSaleDialog.setTitle("Reporting Sale...");
        reportingSaleDialog.setMessage("Please wait while the sale is being reported.");
        reportingSaleDialog.setCancelable(false);
        reportingSaleDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/report_sale";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                reportingSaleDialog.dismiss();
                try {
                    JSONObject reportSaleRes = new JSONObject(response);
                    boolean reportSaleSuccess = Boolean.parseBoolean(reportSaleRes.getString("status"));
                    String reportSaleMessage = reportSaleRes.getString("message");

                    if(reportSaleSuccess) {

                        reportSaleStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportSaleStatusAlert.setTitle("Sale reported successfully");
                        reportSaleStatusAlert.setMessage(reportSaleMessage);
                        reportSaleStatusAlert.setIcon(getResources().getDrawable(R.drawable.success_icon));
                        reportSaleStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportSaleStatusAlert.show();

                    } else {
                        reportSaleStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportSaleStatusAlert.setTitle("Failed to report sale");
                        reportSaleStatusAlert.setMessage(reportSaleMessage);
                        reportSaleStatusAlert.setIcon(getResources().getDrawable(R.drawable.failure_icon));
                        reportSaleStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportSaleStatusAlert.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                signOut();
                Toast.makeText(getActivity(), "Cannot reach cloud servers.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                params.put("region", userDetails.getString("region", null));
                params.put("territory", userDetails.getString("territory", null));
                params.put("date", date_ET.getText().toString());
                params.put("showroom_dealer", "");
                params.put("showroom_dealer_fk", ((CommonStruct) showroom_dealer_SP.getSelectedItem()).getId());
                params.put("chassis_no", chassis_number_ET.getText().toString());
                params.put("customer_name", customer_name_ET.getText().toString());
                params.put("customer_address", customer_address_ET.getText().toString());
                params.put("customer_contact", customer_contact_number_ET.getText().toString());
                params.put("model", ((CommonStruct) model_SP.getSelectedItem()).getId());
                params.put("invoice_no", invoice_number_ET.getText().toString());
                params.put("price", price_ET.getText().toString());
                params.put("sale_type", ((CommonStruct) sale_type_SP.getSelectedItem()).getId());
                params.put("institute", institute_ET.getText().toString());
                params.put("advance", advance_ET.getText().toString());
                params.put("latitude", "0.0");
                params.put("longitude", "0.0");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mQueue.add(request);
    }

    private void validateAPIKey() {

        String url = "https://www.randeepa.cloud/android-api6/validate_api_key";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject apiKeyRes = new JSONObject(response);
                    boolean apiKeyValid = Boolean.parseBoolean(apiKeyRes.getString("status"));

                    if(apiKeyValid) {
                    } else {
                        signOut();
                        Toast.makeText(getActivity(), "Please sign in again", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                signOut();
                Toast.makeText(getActivity(), "Cannot reach cloud servers.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        mQueue.add(request);
    }

    private void signOut() {
        SharedPreferences.Editor userEditor = userDetails.edit();
        userEditor.clear();
        userEditor.commit();

        Intent mainActivity = new Intent(getActivity(), MainActivity.class);
        startActivity(mainActivity);

    }
}
