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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class OrganizationalVisitFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Utils utils;

    private EditText date_ET;
    private EditText start_meter_ET;
    private EditText end_meter_ET;
    private EditText organization_ET;
    private EditText location_ET;
    private EditText purpose_ET;
    private EditText outcome_ET;
    private EditText contact_person_ET;
    private EditText contact_person_number_ET;

    private Spinner organization_type_SP;
    private Spinner organization_SP;

    private Button add_stock_BT;
    private Button add_inquiry_BT;
    private Button report_organizational_visit_BT;

    private ProgressDialog organizationTypeDialog;
    private ProgressDialog organizationDialog;
    private ProgressDialog loadingModelsDialog;

    private ArrayList<CommonStruct> organizationTypes;
    private ArrayList<CommonStruct> organizations;
    private ArrayList<CommonStruct> models;

    private AlertDialog reportOrganizationalVisitStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    private TableLayout stocks_TL;
    private TableLayout inquiries_TL;

    int spinnerCheck = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View organizationalVisit = inflater.inflate(R.layout.fragment_organizational_visit, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        validateAPIKey();

        date_ET = organizationalVisit.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        organization_type_SP = organizationalVisit.findViewById(R.id.organization_type_SP);
        organizationTypes = new ArrayList<>();
        loadSpinner("organizationTypes");

        organization_SP = organizationalVisit.findViewById(R.id.organization_SP);
        organizations = new ArrayList<>();

        organization_type_SP.setOnItemSelectedListener(this);

        start_meter_ET = organizationalVisit.findViewById(R.id.start_meter_ET);
        end_meter_ET = organizationalVisit.findViewById(R.id.end_meter_ET);
        organization_ET = organizationalVisit.findViewById(R.id.organization_ET);
        location_ET = organizationalVisit.findViewById(R.id.location_ET);
        purpose_ET = organizationalVisit.findViewById(R.id.purpose_ET);
        outcome_ET = organizationalVisit.findViewById(R.id.outcome_ET);
        contact_person_ET = organizationalVisit.findViewById(R.id.contact_person_ET);
        contact_person_number_ET = organizationalVisit.findViewById(R.id.contact_person_number_ET);

        add_stock_BT = organizationalVisit.findViewById(R.id.add_stock_BT);
        add_inquiry_BT = organizationalVisit.findViewById(R.id.add_inquiry_BT);
        report_organizational_visit_BT = organizationalVisit.findViewById(R.id.report_organizational_visit_BT);

        stocks_TL = organizationalVisit.findViewById(R.id.stocks_TL);
        inquiries_TL = organizationalVisit.findViewById(R.id.inquiries_TL);

        add_stock_BT.setOnClickListener(this);
        add_inquiry_BT.setOnClickListener(this);
        report_organizational_visit_BT.setOnClickListener(this);

        models = new ArrayList<>();

        return organizationalVisit;
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
            case R.id.add_stock_BT:

                final TableRow stocksRow = new TableRow(getActivity());

                Spinner model_SP_STOCKS_TBL = new Spinner(getActivity());

                loadModels(model_SP_STOCKS_TBL);

                EditText chassis_no_ET_STOCKS_TBL = new EditText(getActivity());
                chassis_no_ET_STOCKS_TBL.setHint("NLXXXXXX");

                Button remove_BT_STOCKS_TBL = new Button(getActivity());
                remove_BT_STOCKS_TBL.setText("Remove");

                remove_BT_STOCKS_TBL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stocks_TL.removeView(stocksRow);
                        stocksRow.removeAllViews();
                    }
                });

                stocksRow.addView(model_SP_STOCKS_TBL);
                stocksRow.addView(chassis_no_ET_STOCKS_TBL);
                stocksRow.addView(remove_BT_STOCKS_TBL);

                stocks_TL.addView(stocksRow);

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
            case R.id.report_organizational_visit_BT:

                int validateStocksTable = validateStocksTable();
                int validateInquiriesTable = validateInquiries();

                if(validateStocksTable == 1) {
                    Toast.makeText(getActivity(), "Enter the chassis numbers", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(validateStocksTable == 2) {
                    Toast.makeText(getActivity(), "Duplicate chassis numbers", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                int selectedOragnizationType = Integer.valueOf(((CommonStruct) organization_type_SP.getSelectedItem()).getId());


                if(selectedOragnizationType == 3) {
                    if (TextUtils.isEmpty(organization_ET.getText().toString())) {
                        Toast.makeText(getActivity(), "Enter the organization", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                if (TextUtils.isEmpty(location_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the location", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(purpose_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the purpose", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(outcome_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the outcome", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(contact_person_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the contact person", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(contact_person_number_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the contact person number", Toast.LENGTH_SHORT).show();
                    return;
                }

                reportOrganizationalVisit();

                break;
        }
    }

    private void reportOrganizationalVisit() {
        reportOrganizationalVisitStatusAlert = new ProgressDialog(getActivity());
        reportOrganizationalVisitStatusAlert.setTitle("Reporting Organizational Visit...");
        reportOrganizationalVisitStatusAlert.setMessage("Please wait while the organizational visit is being reported.");
        reportOrganizationalVisitStatusAlert.setCancelable(false);
        reportOrganizationalVisitStatusAlert.show();

        String url = new API().getApiLink() + "/organizational_visit";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                reportOrganizationalVisitStatusAlert.dismiss();
                try {
                    JSONObject reportOrganizationalVisitRes = new JSONObject(response);
                    boolean reportOrganizationalVisitSuccess = Boolean.parseBoolean(reportOrganizationalVisitRes.getString("status"));
                    String reportOrganizationalVisitMessage = reportOrganizationalVisitRes.getString("message");

                    if(reportOrganizationalVisitSuccess) {

                        reportOrganizationalVisitStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportOrganizationalVisitStatusAlert.setTitle("Organizational visit reported successfully");
                        reportOrganizationalVisitStatusAlert.setMessage(reportOrganizationalVisitMessage);
                        reportOrganizationalVisitStatusAlert.setIcon(getResources().getDrawable(R.drawable.success_icon));
                        reportOrganizationalVisitStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportOrganizationalVisitStatusAlert.show();

                    } else {
                        reportOrganizationalVisitStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportOrganizationalVisitStatusAlert.setTitle("Failed to report organizationl visit");
                        reportOrganizationalVisitStatusAlert.setMessage(reportOrganizationalVisitMessage);
                        reportOrganizationalVisitStatusAlert.setIcon(getResources().getDrawable(R.drawable.failure_icon));
                        reportOrganizationalVisitStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportOrganizationalVisitStatusAlert.show();
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

                int selectedOragnizationType = Integer.valueOf(((CommonStruct) organization_type_SP.getSelectedItem()).getId());

                params.put("organization_type_id", String.valueOf(selectedOragnizationType));

                if(selectedOragnizationType == 3) {
                    params.put("organization_name", organization_ET.getText().toString());
                    params.put("organization_name_fk", String.valueOf(1));
                } else {
                    params.put("organization_name", "");
                    params.put("organization_name_fk", ((CommonStruct) organization_SP.getSelectedItem()).getId());
                }

                params.put("location", location_ET.getText().toString());
                params.put("purpose", purpose_ET.getText().toString());
                params.put("outcome", outcome_ET.getText().toString());
                params.put("contact_person", contact_person_ET.getText().toString());
                params.put("contact_number", contact_person_number_ET.getText().toString());
                params.put("latitude", "0.0");
                params.put("longitude", "0.0");
                params.put("stocks", stocksTableValues().toString());
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

    private JSONArray stocksTableValues() {
        JSONArray stocksArray = new JSONArray();

        if(stocks_TL.getChildCount() == 1) {
            return stocksArray;
        } else {
            for(int i = 1; i < stocks_TL.getChildCount(); i++) {
                TableRow stocksTableRow = (TableRow) stocks_TL.getChildAt(i);

                JSONObject stockObj = new JSONObject();

                Spinner model_SP_STOCKS_TBL = (Spinner) stocksTableRow.getChildAt(0);
                EditText chassis_no_ET_STOCKS_TBL = (EditText) stocksTableRow.getChildAt(1);

                try {
                    stockObj.put("model_id", ((CommonStruct) model_SP_STOCKS_TBL.getSelectedItem()).getId());
                    stockObj.put("chassis_no", chassis_no_ET_STOCKS_TBL.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                stocksArray.put(stockObj);
            }

            return stocksArray;
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

    private int validateStocksTable() {
        if(stocks_TL.getChildCount() == 1) {
            return 0;
        } else {

            String[] chassisNumbers = new String[stocks_TL.getChildCount() - 1];

            for(int i = 1; i < stocks_TL.getChildCount(); i++) {

                TableRow stocksTableRow = (TableRow) stocks_TL.getChildAt(i);

                TextView chassis_no_ET_STOCKS_TBL = (TextView) stocksTableRow.getChildAt(1);

                String chassis_no_TBL = chassis_no_ET_STOCKS_TBL.getText().toString();

                if(TextUtils.isEmpty(chassis_no_TBL)) {
                    return 1;
                }

                if(Arrays.asList(chassisNumbers).contains(chassis_no_TBL)) {
                    return 2;
                } else {
                    chassisNumbers[i-1] = chassis_no_TBL;
                }

            }

            return 0;
        }
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

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "organizationTypes":
                url = new API().getApiLink() + "/organizational_visit_organization_type";
                organizationTypeDialog = new ProgressDialog(getActivity());
                organizationTypeDialog.setTitle("Loading Organizational Types List...");
                organizationTypeDialog.setMessage("Please wait while the organizational types list is loaded.");
                organizationTypeDialog.setCancelable(false);
                organizationTypeDialog.show();
                break;
            case "organizations":
                url = new API().getApiLink() + "/get_organizations";
                organizationDialog = new ProgressDialog(getActivity());
                organizationDialog.setTitle("Loading Organizations List...");
                organizationDialog.setMessage("Please wait while the organizations list is loaded.");
                organizationDialog.setCancelable(false);
                organizationDialog.show();
                organizations.clear();
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
                                case "organizationTypes":
                                    organizationTypes.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                                case "organizations":
                                    organizations.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                                    break;
                            }
                        }

                        switch (name) {
                            case "organizationTypes":
                                ArrayAdapter<CommonStruct> organizationTypesArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, organizationTypes);
                                organization_type_SP.setAdapter(organizationTypesArrayAdapter);
                                organizationTypeDialog.dismiss();
                                loadSpinner("organizations");
                                break;
                            case "organizations":
                                ArrayAdapter<CommonStruct> organizationsArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, organizations);
                                organization_SP.setAdapter(organizationsArrayAdapter);
                                organizationDialog.dismiss();
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

                if(name == "organizations") {
                    params.put("organization_type", ((CommonStruct) organization_type_SP.getSelectedItem()).getId());
                }

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

        models.clear();

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.organization_type_SP:
                if(++spinnerCheck > 1) {
                    int selectedOragnizationType = Integer.valueOf(((CommonStruct) parent.getItemAtPosition(position)).getId());


                    if(selectedOragnizationType == 3) {
                        Log.d("SELECTION", "OTHER SELECTED");
                        organization_SP.setVisibility(View.GONE);
                        organization_ET.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("SELECTION", "OTHER NOT SELECTED");
                        organization_ET.setVisibility(View.GONE);
                        organization_SP.setVisibility(View.VISIBLE);
                        loadSpinner("organizations");
                    }

                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
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
