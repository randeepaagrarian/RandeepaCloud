package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ClaimExpenseFragment extends Fragment implements View.OnClickListener {

    private Utils utils;

    private EditText date_ET;
    private EditText description_ET;

    private Button add_expense_item_BT;
    private Button claim_expense_BT;

    private ProgressDialog expenseTypesDailog;

    private ArrayList<CommonStruct> expenseTypes;

    private AlertDialog claimExpenseStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    private TableLayout expense_items_TL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View claimExpense = inflater.inflate(R.layout.fragment_claim_expense, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        date_ET = claimExpense.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        description_ET = claimExpense.findViewById(R.id.description_ET);

        add_expense_item_BT = claimExpense.findViewById(R.id.add_expense_item_BT);
        claim_expense_BT = claimExpense.findViewById(R.id.claim_expense_BT);

        expense_items_TL = claimExpense.findViewById(R.id.expense_items_TL);

        add_expense_item_BT.setOnClickListener(this);
        claim_expense_BT.setOnClickListener(this);

        expenseTypes = new ArrayList<>();

        return claimExpense;
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
            case R.id.add_expense_item_BT:

                final TableRow expenseItemsRow = new TableRow(getActivity());

                Spinner expense_type_SP_EXPENSE_ITEMS_TBL = new Spinner(getActivity());

                loadExpenseTypes(expense_type_SP_EXPENSE_ITEMS_TBL);

                EditText amount_ET_EXPENSE_ITEMS_TBL = new EditText(getActivity());
                amount_ET_EXPENSE_ITEMS_TBL.setHint("10000");
                amount_ET_EXPENSE_ITEMS_TBL.setInputType(InputType.TYPE_CLASS_NUMBER);

                EditText description_ET_EXPENSE_ITEMS_TBL = new EditText(getActivity());

                Button remove_BT_EXPENSE_ITEMS_TBL = new Button(getActivity());
                remove_BT_EXPENSE_ITEMS_TBL.setText("Remove");

                remove_BT_EXPENSE_ITEMS_TBL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expense_items_TL.removeView(expenseItemsRow);
                        expenseItemsRow.removeAllViews();
                    }
                });

                expenseItemsRow.addView(expense_type_SP_EXPENSE_ITEMS_TBL);
                expenseItemsRow.addView(amount_ET_EXPENSE_ITEMS_TBL);
                expenseItemsRow.addView(description_ET_EXPENSE_ITEMS_TBL);
                expenseItemsRow.addView(remove_BT_EXPENSE_ITEMS_TBL);

                expense_items_TL.addView(expenseItemsRow);

                break;
            case R.id.claim_expense_BT:

                int validateExpenseItemsTable = validateExpenseItemsTable();

                if(validateExpenseItemsTable == 1) {
                    Toast.makeText(getActivity(), "Enter the expense amounts", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(validateExpenseItemsTable == 2) {
                    Toast.makeText(getActivity(), "Enter the expenses", Toast.LENGTH_SHORT).show();
                    return;
                }

                claimExpense();

                break;
        }
    }

    private void claimExpense() {
        claimExpenseStatusAlert = new ProgressDialog(getActivity());
        claimExpenseStatusAlert.setTitle("Claiming Expense...");
        claimExpenseStatusAlert.setMessage("Please wait while the expense is being claimed.");
        claimExpenseStatusAlert.setCancelable(false);
        claimExpenseStatusAlert.show();

        String url = "https://www.randeepa.cloud/android-api6/claim_expense";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                claimExpenseStatusAlert.dismiss();
                try {
                    JSONObject claimExpenseRes = new JSONObject(response);
                    boolean claimExpenseSuccess = Boolean.parseBoolean(claimExpenseRes.getString("status"));
                    String claimExpenseMessage = claimExpenseRes.getString("message");

                    if(claimExpenseSuccess) {

                        claimExpenseStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        claimExpenseStatusAlert.setTitle("Expense Claimed Successfully");
                        claimExpenseStatusAlert.setMessage(claimExpenseMessage);
                        claimExpenseStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        claimExpenseStatusAlert.show();

                    } else {
                        claimExpenseStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        claimExpenseStatusAlert.setTitle("Failed to Claim Expense");
                        claimExpenseStatusAlert.setMessage(claimExpenseMessage);
                        claimExpenseStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        claimExpenseStatusAlert.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                claimExpenseStatusAlert.dismiss();
                error.printStackTrace();
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
                params.put("description", description_ET.getText().toString());
                params.put("expense_items", expenseItemsTableValues().toString());
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

        mQueue.add(request);
    }

    private JSONArray expenseItemsTableValues() {
        JSONArray expenseItemsArray = new JSONArray();

        if(expense_items_TL.getChildCount() == 1) {
            return expenseItemsArray;
        } else {
            for(int i = 1; i < expense_items_TL.getChildCount(); i++) {
                TableRow expenseItemsTableRow = (TableRow) expense_items_TL.getChildAt(i);

                JSONObject expenseItemObj = new JSONObject();

                Spinner expense_type_SP_EXPENSE_ITEMS_TBL = (Spinner) expenseItemsTableRow.getChildAt(0);
                EditText amount_ET_EXPENSE_ITEMS_TBL = (EditText) expenseItemsTableRow.getChildAt(1);
                EditText description_ET_EXPENSE_ITEMS_TBL = (EditText) expenseItemsTableRow.getChildAt(2);

                try {
                    expenseItemObj.put("expense_type_id", ((CommonStruct) expense_type_SP_EXPENSE_ITEMS_TBL.getSelectedItem()).getId());
                    expenseItemObj.put("amount", amount_ET_EXPENSE_ITEMS_TBL.getText().toString());
                    expenseItemObj.put("description", description_ET_EXPENSE_ITEMS_TBL.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                expenseItemsArray.put(expenseItemObj);
            }

            return expenseItemsArray;
        }

    }

    private int validateExpenseItemsTable() {
        if(expense_items_TL.getChildCount() == 1) {
            return 2;
        } else {

            for(int i = 1; i < expense_items_TL.getChildCount(); i++) {

                TableRow expenseItemsTableRow = (TableRow) expense_items_TL.getChildAt(i);

                TextView amount_ET_EXPENSE_ITEMS_TBL = (TextView) expenseItemsTableRow.getChildAt(1);

                if(TextUtils.isEmpty(amount_ET_EXPENSE_ITEMS_TBL.getText().toString())) {
                    return 1;
                }

            }

            return 0;
        }
    }

    private void loadExpenseTypes(final Spinner spinner) {

        String url = "https://www.randeepa.cloud/android-api6/expense_type";

        expenseTypesDailog = new ProgressDialog(getActivity());
        expenseTypesDailog.setTitle("Loading Expense Types...");
        expenseTypesDailog.setMessage("Please wait while expense types are loaded.");
        expenseTypesDailog.setCancelable(false);
        expenseTypesDailog.show();

        expenseTypes.clear();

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

                            expenseTypes.add(new CommonStruct(spinnerResArrayItem.getString("id"), spinnerResArrayItem.getString("name")));
                        }

                        ArrayAdapter<CommonStruct> expenseTypesArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, expenseTypes);
                        spinner.setAdapter(expenseTypesArrayAdapter);
                        expenseTypesDailog.dismiss();
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
