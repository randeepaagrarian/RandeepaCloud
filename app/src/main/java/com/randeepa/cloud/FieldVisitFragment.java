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
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.randeepa.cloud.api.API;
import com.randeepa.cloud.structs.CommonStruct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FieldVisitFragment extends Fragment implements View.OnClickListener {

    private Utils utils;

    private EditText date_ET;
    private EditText start_meter_ET;
    private EditText end_meter_ET;
    private EditText location_ET;

    private Spinner field_visit_criteria_SP;

    private Button add_inquiry_BT;
    private Button report_field_visit_BT;

    private ProgressDialog fieldVisitCriteriaDialog;
    private ProgressDialog loadingModelsDialog;

    private ArrayList<CommonStruct> fieldVisitCriterias;
    private ArrayList<CommonStruct> models;

    private AlertDialog reportFieldVisitStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    private TableLayout inquiries_TL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fieldVisit = inflater.inflate(R.layout.fragment_field_visit, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        validateAPIKey();

        date_ET = fieldVisit.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        field_visit_criteria_SP = fieldVisit.findViewById(R.id.field_visit_criteria_SP);
        fieldVisitCriterias = new ArrayList<>();
        loadSpinner("fieldVisitCriterias");

        start_meter_ET = fieldVisit.findViewById(R.id.start_meter_ET);
        end_meter_ET = fieldVisit.findViewById(R.id.end_meter_ET);
        location_ET = fieldVisit.findViewById(R.id.location_ET);

        add_inquiry_BT = fieldVisit.findViewById(R.id.add_inquiry_BT);
        report_field_visit_BT = fieldVisit.findViewById(R.id.report_field_visit_BT);

        add_inquiry_BT.setOnClickListener(this);
        report_field_visit_BT.setOnClickListener(this);

        inquiries_TL = fieldVisit.findViewById(R.id.inquiries_TL);

        models = new ArrayList<>();

        return fieldVisit;
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
            case R.id.add_inquiry_BT:

                final TableRow inquiryRow = new TableRow(getActivity());

                EditText name_ET_INQUIRY_TBL = new EditText(getActivity());
                name_ET_INQUIRY_TBL.setHint("Nimal Bandara");

                EditText nic_ET_INQUIRY_TBL = new EditText(getActivity());
                nic_ET_INQUIRY_TBL.setHint("000000000v");

                EditText contact_no_ET_INQUIRY_TBL = new EditText(getActivity());
                contact_no_ET_INQUIRY_TBL.setHint("0777777777");
                contact_no_ET_INQUIRY_TBL.setInputType(InputType.TYPE_CLASS_NUMBER);

                Spinner model_SP_INQUIRY_TBL = new Spinner(getActivity());

                loadModels(model_SP_INQUIRY_TBL);

                EditText address_ET_INQUIRY_TBL = new EditText(getActivity());
                address_ET_INQUIRY_TBL.setHint("No: 1598, Polonnaruwa");

                EditText inquiry_ET_INQUIRY_TBL = new EditText(getActivity());
                inquiry_ET_INQUIRY_TBL.setHint("Capacity, fuel");


                Button remove_BT_INQUIRY_TBL = new Button(getActivity());
                remove_BT_INQUIRY_TBL.setText("Remove");

                remove_BT_INQUIRY_TBL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inquiries_TL.removeView(inquiryRow);
                        inquiryRow.removeAllViews();
                    }
                });

                inquiryRow.addView(name_ET_INQUIRY_TBL);
                inquiryRow.addView(nic_ET_INQUIRY_TBL);
                inquiryRow.addView(contact_no_ET_INQUIRY_TBL);
                inquiryRow.addView(model_SP_INQUIRY_TBL);
                inquiryRow.addView(address_ET_INQUIRY_TBL);
                inquiryRow.addView(inquiry_ET_INQUIRY_TBL);
                inquiryRow.addView(remove_BT_INQUIRY_TBL);

                inquiries_TL.addView(inquiryRow);

                break;
            case R.id.report_field_visit_BT:
                int validateInquiriesTable = validateInquiries();

                if(validateInquiriesTable == 1) {
                    Toast.makeText(getActivity(), "Complete the inquiries table", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(date_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(start_meter_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the start meter", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(end_meter_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the end meter", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(location_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the location", Toast.LENGTH_SHORT).show();
                    return;
                }

                reportFieldVisit();

                break;
        }
    }

    private void reportFieldVisit() {
        reportFieldVisitStatusAlert = new ProgressDialog(getActivity());
        reportFieldVisitStatusAlert.setTitle("Reporting Field Visit...");
        reportFieldVisitStatusAlert.setMessage("Please wait while the field visit is being reported.");
        reportFieldVisitStatusAlert.setCancelable(false);
        reportFieldVisitStatusAlert.show();

        String url = new API().getApiLink() + "/field_visit";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                reportFieldVisitStatusAlert.dismiss();
                try {
                    JSONObject reportFieldVisitRes = new JSONObject(response);
                    boolean reportFieldVisitSuccess = Boolean.parseBoolean(reportFieldVisitRes.getString("status"));
                    String reportFieldVisitMessage = reportFieldVisitRes.getString("message");

                    if(reportFieldVisitSuccess) {

                        reportFieldVisitStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportFieldVisitStatusAlert.setTitle("Field visit reported successfully");
                        reportFieldVisitStatusAlert.setMessage(reportFieldVisitMessage);
                        reportFieldVisitStatusAlert.setIcon(getResources().getDrawable(R.drawable.success_icon));
                        reportFieldVisitStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportFieldVisitStatusAlert.show();

                    } else {
                        reportFieldVisitStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportFieldVisitStatusAlert.setTitle("Failed to report field visit");
                        reportFieldVisitStatusAlert.setMessage(reportFieldVisitMessage);
                        reportFieldVisitStatusAlert.setIcon(getResources().getDrawable(R.drawable.failure_icon));
                        reportFieldVisitStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportFieldVisitStatusAlert.show();
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
                params.put("start_meter", start_meter_ET.getText().toString());
                params.put("end_meter", end_meter_ET.getText().toString());
                params.put("location", location_ET.getText().toString());
                params.put("field_visit_criteria_id", ((CommonStruct) field_visit_criteria_SP.getSelectedItem()).getId());
                params.put("latitude", "0.0");
                params.put("longitude", "0.0");
                params.put("inquiries", inquiriesTableValues().toString());
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

    private int validateInquiries() {
        if(inquiries_TL.getChildCount() == 1) {
            return 0;
        } else {

            for(int i = 1; i < inquiries_TL.getChildCount(); i++) {

                TableRow inquiriesTableRow = (TableRow) inquiries_TL.getChildAt(i);

                TextView name_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(0);
                TextView nic_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(1);
                TextView contact_no_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(2);
                TextView address_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(4);
                TextView inquiry_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(5);

                if(TextUtils.isEmpty(name_ET_INQUIRY_TBL.getText().toString())
                        || TextUtils.isEmpty(nic_ET_INQUIRY_TBL.getText().toString())
                        || TextUtils.isEmpty(contact_no_ET_INQUIRY_TBL.getText().toString())
                        || TextUtils.isEmpty(address_ET_INQUIRY_TBL.getText().toString())
                        || TextUtils.isEmpty(inquiry_ET_INQUIRY_TBL.getText().toString())) {
                    return 1;
                }


            }

            return 0;
        }
    }

    private JSONArray inquiriesTableValues() {
        JSONArray inquiriesArray = new JSONArray();

        if(inquiries_TL.getChildCount() == 1) {
            return inquiriesArray;
        } else {
            for(int i = 1; i < inquiries_TL.getChildCount(); i++) {
                TableRow inquiriesTableRow = (TableRow) inquiries_TL.getChildAt(i);

                JSONObject inquiryObj = new JSONObject();

                TextView name_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(0);
                TextView nic_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(1);
                TextView contact_no_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(2);
                Spinner model_SP_INQUIRY_TBL = (Spinner) inquiriesTableRow.getChildAt(3);
                TextView address_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(4);
                TextView inquiry_ET_INQUIRY_TBL = (TextView) inquiriesTableRow.getChildAt(5);

                try {

                    inquiryObj.put("customer_name", name_ET_INQUIRY_TBL.getText().toString());
                    inquiryObj.put("customer_nic", nic_ET_INQUIRY_TBL.getText().toString());
                    inquiryObj.put("customer_telephone", contact_no_ET_INQUIRY_TBL.getText().toString());
                    inquiryObj.put("model", ((CommonStruct) model_SP_INQUIRY_TBL.getSelectedItem()).getId());
                    inquiryObj.put("customer_address", address_ET_INQUIRY_TBL.getText().toString());
                    inquiryObj.put("inquiry", inquiry_ET_INQUIRY_TBL.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                inquiriesArray.put(inquiryObj);
            }

            return inquiriesArray;
        }

    }

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "fieldVisitCriterias":
                url = new API().getApiLink() + "/field_visit_criteria";
                fieldVisitCriteriaDialog = new ProgressDialog(getActivity());
                fieldVisitCriteriaDialog.setTitle("Loading Field Visit Criterias...");
                fieldVisitCriteriaDialog.setMessage("Please wait while field visit criterias are loaded.");
                fieldVisitCriteriaDialog.setCancelable(false);
                fieldVisitCriteriaDialog.show();
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
                                case "fieldVisitCriterias":
                                    fieldVisitCriterias.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                            }
                        }

                        switch (name) {
                            case "fieldVisitCriterias":
                                ArrayAdapter<CommonStruct> fieldVisitCriteriaArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, fieldVisitCriterias);
                                field_visit_criteria_SP.setAdapter(fieldVisitCriteriaArrayAdapter);
                                fieldVisitCriteriaDialog.dismiss();
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

    private void loadModels(final Spinner spinner) {

        String url = new API().getApiLink() + "/sale_report_models";

        loadingModelsDialog = new ProgressDialog(getActivity());
        loadingModelsDialog.setTitle("Loading Models...");
        loadingModelsDialog.setMessage("Please wait while models are loaded.");
        loadingModelsDialog.setCancelable(false);
        loadingModelsDialog.show();

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

                            models.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                        }

                        ArrayAdapter<CommonStruct> showroomDealerArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, models);
                        spinner.setAdapter(showroomDealerArrayAdapter);
                        loadingModelsDialog.dismiss();
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

    private void validateAPIKey() {

        String url = new API().getApiLink() + "/validate_api_key";

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
