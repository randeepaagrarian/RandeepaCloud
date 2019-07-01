package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private ProgressDialog addStockDialog;
    private ProgressDialog addInquiryDialog;

    private ArrayList<CommonStruct> organizationTypes;
    private ArrayList<CommonStruct> organizations;
    private ArrayList<CommonStruct> models;

    private AlertDialog reportOrganizationalVisitStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    int spinnerCheck = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View organizationalVisit = inflater.inflate(R.layout.fragment_organizational_visit, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

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

        add_stock_BT.setOnClickListener(this);

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
                
                break;
        }
    }

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "organizationTypes":
                url = "https://www.randeepa.cloud/android-api6/organizational_visit_organization_type";
                organizationTypeDialog = new ProgressDialog(getActivity());
                organizationTypeDialog.setTitle("Loading Organizational Types List...");
                organizationTypeDialog.setMessage("Please wait while the organizational types list is loaded.");
                organizationTypeDialog.setCancelable(false);
                organizationTypeDialog.show();
                break;
            case "organizations":
                url = "https://www.randeepa.cloud/android-api6/get_organizations";
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
}
