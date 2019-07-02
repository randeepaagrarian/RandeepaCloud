package com.randeepa.cloud;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {

    private ProgressDialog loadSalesDialog;
    private ProgressDialog loadBankingsDialog;
    private ProgressDialog loadOrganizationalVisitsDialog;
    private ProgressDialog loadFieldVisitsDialog;
    private ProgressDialog loadExpenseClaimsDialog;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    private TableLayout recent_sales_TL;
    private TableLayout recent_bankings_TL;
    private TableLayout recent_organizational_visits_TL;
    private TableLayout recent_field_visits_TL;
    private TableLayout recent_expense_claims_TL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dashboard = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        recent_sales_TL = dashboard.findViewById(R.id.recent_sales_TL);
        recent_bankings_TL = dashboard.findViewById(R.id.recent_bankings_TL);
        recent_organizational_visits_TL = dashboard.findViewById(R.id.recent_organizational_visits_TL);
        recent_field_visits_TL = dashboard.findViewById(R.id.recent_field_visits_TL);
        recent_expense_claims_TL = dashboard.findViewById(R.id.recent_expense_claims_TL);

        loadRecentSales();
        loadRecentBankings();
        loadRecentOrganizationalVisits();
        loadRecentFieldVisits();
        loadRecentExpenseClaims();

        return dashboard;
    }

    private void loadRecentSales() {

        loadSalesDialog = new ProgressDialog(getActivity());
        loadSalesDialog.setTitle("Loading Recent Sales");
        loadSalesDialog.setMessage("Please wait while recent sales are loaded");
        loadSalesDialog.setCancelable(false);
        loadSalesDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/recent_sales";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadSalesDialog.dismiss();
                try {

                    JSONObject recentSalesRes = new JSONObject(response);
                    boolean recentSalesSuccess = Boolean.parseBoolean(recentSalesRes.getString("status"));

                    if(recentSalesSuccess) {

                        JSONArray recentSales = new JSONArray(recentSalesRes.getString("message"));

                        for(int i = 0; i < recentSales.length(); i++) {
                            JSONObject sale = recentSales.getJSONObject(i);

                            final TableRow saleRow = new TableRow(getActivity());
                            saleRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            saleRow.setPadding(5, 5, 5, 5);

                            TextView date_TV_SALE_TBL = new TextView(getActivity());
                            date_TV_SALE_TBL.setText(sale.getString("sys_date"));
                            date_TV_SALE_TBL.setPadding(10, 10, 10, 10);


                            TextView showroom_dealer_TV_SALE_TBL = new TextView(getActivity());
                            showroom_dealer_TV_SALE_TBL.setText(sale.getString("location"));
                            showroom_dealer_TV_SALE_TBL.setPadding(10, 10, 10, 10);


                            TextView model_TV_SALE_TBL = new TextView(getActivity());
                            model_TV_SALE_TBL.setText(sale.getString("name"));
                            model_TV_SALE_TBL.setPadding(10, 10, 10, 10);


                            TextView chassis_no_TV_SALE_TBL = new TextView(getActivity());
                            chassis_no_TV_SALE_TBL.setText(sale.getString("chassis_no"));
                            chassis_no_TV_SALE_TBL.setPadding(10, 10, 10, 10);


                            TextView customer_name_TV_SALE_TBL = new TextView(getActivity());
                            customer_name_TV_SALE_TBL.setText(sale.getString("customer_name"));
                            customer_name_TV_SALE_TBL.setPadding(10, 10, 10, 10);


                            saleRow.addView(date_TV_SALE_TBL);
                            saleRow.addView(showroom_dealer_TV_SALE_TBL);
                            saleRow.addView(model_TV_SALE_TBL);
                            saleRow.addView(chassis_no_TV_SALE_TBL);
                            saleRow.addView(customer_name_TV_SALE_TBL);

                            recent_sales_TL.addView(saleRow);

                        }

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadSalesDialog.dismiss();
                error.printStackTrace();
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

    private void loadRecentBankings() {

        loadBankingsDialog = new ProgressDialog(getActivity());
        loadBankingsDialog.setTitle("Loading Recent Bankings");
        loadBankingsDialog.setMessage("Please wait while recent bankings are loaded");
        loadBankingsDialog.setCancelable(false);
        loadBankingsDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/recent_bankings";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadBankingsDialog.dismiss();
                try {

                    JSONObject recentBankingsRes = new JSONObject(response);
                    boolean recentBankingsSuccess = Boolean.parseBoolean(recentBankingsRes.getString("status"));

                    if(recentBankingsSuccess) {

                        JSONArray recentBankings = new JSONArray(recentBankingsRes.getString("message"));

                        for(int i = 0; i < recentBankings.length(); i++) {
                            JSONObject sale = recentBankings.getJSONObject(i);

                            final TableRow bankingRow = new TableRow(getActivity());
                            bankingRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            bankingRow.setPadding(5, 5, 5, 5);

                            TextView date_TV_BANKING_TBL = new TextView(getActivity());
                            date_TV_BANKING_TBL.setText(sale.getString("sys_date"));
                            date_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView bank_TV_BANKING_TBL = new TextView(getActivity());
                            bank_TV_BANKING_TBL.setText(sale.getString("bank"));
                            bank_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView branch_TV_BANKING_TBL = new TextView(getActivity());
                            branch_TV_BANKING_TBL.setText(sale.getString("branch"));
                            branch_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView amount_TV_BANKING_TBL = new TextView(getActivity());
                            amount_TV_BANKING_TBL.setText(sale.getString("amount"));
                            amount_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView chassis_no_TV_BANKING_TBL = new TextView(getActivity());
                            chassis_no_TV_BANKING_TBL.setText(sale.getString("chassis_no"));
                            chassis_no_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView invoice_no_TV_BANKING_TBL = new TextView(getActivity());
                            invoice_no_TV_BANKING_TBL.setText(sale.getString("invoice_number"));
                            invoice_no_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            TextView receipt_no_TV_BANKING_TBL = new TextView(getActivity());
                            receipt_no_TV_BANKING_TBL.setText(sale.getString("receipt_number"));
                            receipt_no_TV_BANKING_TBL.setPadding(10, 10, 10, 10);

                            bankingRow.addView(date_TV_BANKING_TBL);
                            bankingRow.addView(bank_TV_BANKING_TBL);
                            bankingRow.addView(branch_TV_BANKING_TBL);
                            bankingRow.addView(amount_TV_BANKING_TBL);
                            bankingRow.addView(chassis_no_TV_BANKING_TBL);
                            bankingRow.addView(invoice_no_TV_BANKING_TBL);
                            bankingRow.addView(receipt_no_TV_BANKING_TBL);

                            recent_bankings_TL.addView(bankingRow);

                        }

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadBankingsDialog.dismiss();
                error.printStackTrace();
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

    private void loadRecentOrganizationalVisits() {

        loadOrganizationalVisitsDialog = new ProgressDialog(getActivity());
        loadOrganizationalVisitsDialog.setTitle("Loading Recent Organizational Visits");
        loadOrganizationalVisitsDialog.setMessage("Please wait while recent organizational visits are loaded");
        loadOrganizationalVisitsDialog.setCancelable(false);
        loadOrganizationalVisitsDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/recent_organizational_visits";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadOrganizationalVisitsDialog.dismiss();
                try {

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean reponseSuccess = Boolean.parseBoolean(responseOBJ.getString("status"));

                    if(reponseSuccess) {

                        JSONArray responseArray = new JSONArray(responseOBJ.getString("message"));

                        for(int i = 0; i < responseArray.length(); i++) {
                            JSONObject itemOBJ = responseArray.getJSONObject(i);

                            final TableRow tableRow = new TableRow(getActivity());
                            tableRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            tableRow.setPadding(5, 5, 5, 5);

                            TextView date_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            date_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("sys_date"));
                            date_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView type_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            type_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("name"));
                            type_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView stocks_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            stocks_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("stocks"));
                            stocks_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView inquiries_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            inquiries_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("inquiries"));
                            inquiries_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView purpose_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            purpose_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("purpose"));
                            purpose_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView contact_person_TV_ORGANIZATIONAL_VISIT_TBL = new TextView(getActivity());
                            contact_person_TV_ORGANIZATIONAL_VISIT_TBL.setText(itemOBJ.getString("contact_person"));
                            contact_person_TV_ORGANIZATIONAL_VISIT_TBL.setPadding(10, 10, 10, 10);

                            tableRow.addView(date_TV_ORGANIZATIONAL_VISIT_TBL);
                            tableRow.addView(type_TV_ORGANIZATIONAL_VISIT_TBL);
                            tableRow.addView(stocks_TV_ORGANIZATIONAL_VISIT_TBL);
                            tableRow.addView(inquiries_TV_ORGANIZATIONAL_VISIT_TBL);
                            tableRow.addView(purpose_TV_ORGANIZATIONAL_VISIT_TBL);
                            tableRow.addView(contact_person_TV_ORGANIZATIONAL_VISIT_TBL);

                            recent_organizational_visits_TL.addView(tableRow);

                        }

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadOrganizationalVisitsDialog.dismiss();
                error.printStackTrace();
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

    private void loadRecentFieldVisits() {

        loadFieldVisitsDialog = new ProgressDialog(getActivity());
        loadFieldVisitsDialog.setTitle("Loading Recent Field Visits");
        loadFieldVisitsDialog.setMessage("Please wait while recent field visits are loaded");
        loadFieldVisitsDialog.setCancelable(false);
        loadFieldVisitsDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/recent_field_visits";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadFieldVisitsDialog.dismiss();
                try {

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean reponseSuccess = Boolean.parseBoolean(responseOBJ.getString("status"));

                    if(reponseSuccess) {

                        JSONArray responseArray = new JSONArray(responseOBJ.getString("message"));

                        for(int i = 0; i < responseArray.length(); i++) {
                            JSONObject itemOBJ = responseArray.getJSONObject(i);

                            final TableRow tableRow = new TableRow(getActivity());
                            tableRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            tableRow.setPadding(5, 5, 5, 5);

                            TextView date_TV_FIELD_VISIT_TBL = new TextView(getActivity());
                            date_TV_FIELD_VISIT_TBL.setText(itemOBJ.getString("sys_date"));
                            date_TV_FIELD_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView location_TV_FIELD_VISIT_TBL = new TextView(getActivity());
                            location_TV_FIELD_VISIT_TBL.setText(itemOBJ.getString("location"));
                            location_TV_FIELD_VISIT_TBL.setPadding(10, 10, 10, 10);

                            TextView inquiries_TV_FIELD_VISIT_TBL = new TextView(getActivity());
                            inquiries_TV_FIELD_VISIT_TBL.setText(itemOBJ.getString("inquiries"));
                            inquiries_TV_FIELD_VISIT_TBL.setPadding(10, 10, 10, 10);

                            tableRow.addView(date_TV_FIELD_VISIT_TBL);
                            tableRow.addView(location_TV_FIELD_VISIT_TBL);
                            tableRow.addView(inquiries_TV_FIELD_VISIT_TBL);

                            recent_field_visits_TL.addView(tableRow);

                        }

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadFieldVisitsDialog.dismiss();
                error.printStackTrace();
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

    private void loadRecentExpenseClaims() {

        loadExpenseClaimsDialog = new ProgressDialog(getActivity());
        loadExpenseClaimsDialog.setTitle("Loading Recent Expense Claims Visits");
        loadExpenseClaimsDialog.setMessage("Please wait while recent expense claims are loaded");
        loadExpenseClaimsDialog.setCancelable(false);
        loadExpenseClaimsDialog.show();

        String url = "https://www.randeepa.cloud/android-api6/recent_expense_claims";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadExpenseClaimsDialog.dismiss();
                try {

                    JSONObject responseOBJ = new JSONObject(response);
                    boolean reponseSuccess = Boolean.parseBoolean(responseOBJ.getString("status"));

                    if(reponseSuccess) {

                        JSONArray responseArray = new JSONArray(responseOBJ.getString("message"));

                        for(int i = 0; i < responseArray.length(); i++) {
                            JSONObject itemOBJ = responseArray.getJSONObject(i);

                            final TableRow tableRow = new TableRow(getActivity());
                            tableRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            tableRow.setPadding(5, 5, 5, 5);

                            TextView date_TV_EXPENSE_CLAIMS_TBL = new TextView(getActivity());
                            date_TV_EXPENSE_CLAIMS_TBL.setText(itemOBJ.getString("id"));
                            date_TV_EXPENSE_CLAIMS_TBL.setPadding(10, 10, 10, 10);

                            TextView location_TV_EXPENSE_CLAIMS_TBL = new TextView(getActivity());
                            location_TV_EXPENSE_CLAIMS_TBL.setText(itemOBJ.getString("sys_date"));
                            location_TV_EXPENSE_CLAIMS_TBL.setPadding(10, 10, 10, 10);

                            TextView claim_amount_TV_EXPENSE_CLAIMS_TBL = new TextView(getActivity());
                            claim_amount_TV_EXPENSE_CLAIMS_TBL.setText(itemOBJ.getString("expense_amount"));
                            claim_amount_TV_EXPENSE_CLAIMS_TBL.setPadding(10, 10, 10, 10);

                            TextView description_TV_EXPENSE_CLAIMS_TBL = new TextView(getActivity());
                            description_TV_EXPENSE_CLAIMS_TBL.setText(itemOBJ.getString("description"));
                            description_TV_EXPENSE_CLAIMS_TBL.setPadding(10, 10, 10, 10);

                            tableRow.addView(date_TV_EXPENSE_CLAIMS_TBL);
                            tableRow.addView(location_TV_EXPENSE_CLAIMS_TBL);
                            tableRow.addView(claim_amount_TV_EXPENSE_CLAIMS_TBL);
                            tableRow.addView(description_TV_EXPENSE_CLAIMS_TBL);

                            recent_expense_claims_TL.addView(tableRow);

                        }

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadExpenseClaimsDialog.dismiss();
                error.printStackTrace();
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

}
