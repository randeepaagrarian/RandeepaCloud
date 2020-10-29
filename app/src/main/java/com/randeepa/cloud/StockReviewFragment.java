package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class StockReviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Utils utils;

    private Spinner organization_type_SP;
    private Spinner organization_SP;

    private TableLayout stocks_TL;

    private ImageView image_IV;
    private int REQUEST_CODE = 1;

    private ProgressDialog organizationDialog;
    private ProgressDialog loadingStocksDialog;

    private AlertDialog sendStockReviewStatusAlert;

    private Button upload_review_BT;

    private EditText stock_review;

    private ArrayList<CommonStruct> organizationTypes;
    private ArrayList<CommonStruct> organizations;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    int organizationTypeSpinnerCheck = 0;
    int organizationSpinnerCheck = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View stockReview = inflater.inflate(R.layout.fragment_stock_review, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        validateAPIKey();

        organization_type_SP = stockReview.findViewById(R.id.organization_type_SP);
        organization_SP = stockReview.findViewById(R.id.organization_SP);

        stocks_TL = stockReview.findViewById(R.id.stocks_TL);

        organizationTypes = new ArrayList<>();
        organizations = new ArrayList<>();
        loadSpinner("organizationTypes");

        organization_type_SP.setOnItemSelectedListener(this);
        organization_SP.setOnItemSelectedListener(this);

        image_IV = stockReview.findViewById(R.id.image_IV);
        image_IV.setOnClickListener(this);

        stock_review = stockReview.findViewById(R.id.stock_review);

        upload_review_BT = stockReview.findViewById(R.id.upload_review_BT);
        upload_review_BT.setOnClickListener(this);

        return stockReview;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.image_IV:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
                break;
            case R.id.upload_review_BT:

                if (image_IV.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.upload).getConstantState()) {
                    Toast.makeText(getActivity(), "Select the image", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(stock_review.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the review", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendStockReview();

                break;
        }
    }

    private void sendStockReview() {
        sendStockReviewStatusAlert = new ProgressDialog(getActivity());
        sendStockReviewStatusAlert.setTitle("Sending Stock Review...");
        sendStockReviewStatusAlert.setMessage("Please wait while the stock review is being sent.");
        sendStockReviewStatusAlert.setCancelable(false);
        sendStockReviewStatusAlert.show();

        String url = new API().getApiLink() + "/stock_review";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                sendStockReviewStatusAlert.dismiss();
                try {
                    JSONObject reportSalesBankingRes = new JSONObject(response);
                    boolean reportSalesBankingSuccess = Boolean.parseBoolean(reportSalesBankingRes.getString("status"));
                    String reportSalesBankingMessage = reportSalesBankingRes.getString("message");

                    if(reportSalesBankingSuccess) {

                        sendStockReviewStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        sendStockReviewStatusAlert.setTitle("Stock review sent successfully");
                        sendStockReviewStatusAlert.setMessage(reportSalesBankingMessage);
                        sendStockReviewStatusAlert.setIcon(getResources().getDrawable(R.drawable.success_icon));
                        sendStockReviewStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        sendStockReviewStatusAlert.show();

                    } else {
                        sendStockReviewStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        sendStockReviewStatusAlert.setTitle("Failed to send stock review");
                        sendStockReviewStatusAlert.setMessage(reportSalesBankingMessage);
                        sendStockReviewStatusAlert.setIcon(getResources().getDrawable(R.drawable.failure_icon));
                        sendStockReviewStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        sendStockReviewStatusAlert.show();
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

                Bitmap image = ((BitmapDrawable) image_IV.getDrawable()).getBitmap();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 65, outputStream);
                String imageB64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

                Map<String,String> params = new HashMap<String, String>();
                params.put("picture", imageB64);
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                params.put("remark", stock_review.getText().toString());
                params.put("dealer_id", ((CommonStruct) organization_SP.getSelectedItem()).getId());
//                params.put("latitude", "0.0");
////                params.put("longitude", "0.0");
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

    private void loadSpinner(final String name) {

        String url = "";

        switch (name) {
            case "organizationTypes":
                organizationTypes.add(new CommonStruct("6", "Dealer"));
                organizationTypes.add(new CommonStruct("7", "Showroom"));
                ArrayAdapter<CommonStruct> organizationTypesArrayAdapter = new ArrayAdapter<CommonStruct>(getActivity(), android.R.layout.simple_spinner_dropdown_item, organizationTypes);
                organization_type_SP.setAdapter(organizationTypesArrayAdapter);
                loadSpinner("organizations");
                return;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.organization_type_SP:
                if(++organizationTypeSpinnerCheck > 1) {
                    Log.d("SELECTION", "OTHER NOT SELECTED");
                    organization_SP.setVisibility(View.VISIBLE);
                    loadSpinner("organizations");
                }
                break;
            case R.id.organization_SP:
                if(++organizationTypeSpinnerCheck > 1) {
                    CommonStruct selectedItem = (CommonStruct) organization_SP.getSelectedItem();
                    Log.d("LOCATION", selectedItem.getId());
                    loadStocks(selectedItem.getId());
                }
                break;
        }
    }

    private void loadStocks(final String orgID) {
        loadingStocksDialog = new ProgressDialog(getActivity());
        loadingStocksDialog.setTitle("Loading Stocks...");
        loadingStocksDialog.setMessage("Please wait while stocks are loaded.");
        loadingStocksDialog.setCancelable(false);
        loadingStocksDialog.show();

        String url = new API().getApiLink() + "/current_stock";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject stockRes = new JSONObject(response);
                    boolean stockPresent = Boolean.parseBoolean(stockRes.getString("status"));

                    if(stockPresent) {
                        while (stocks_TL.getChildCount() > 1)
                            stocks_TL.removeView(stocks_TL.getChildAt(stocks_TL.getChildCount() - 1));

                        JSONArray stocksArray = stockRes.getJSONArray("message");
                        String []fields = {"primary_id", "secondary_id", "name", "date", "delivery_document_id", "delivery_document_type_name"};
                        for(int i = 0; i < stocksArray.length(); i++) {
                            JSONObject stockItem = stocksArray.getJSONObject(i);

                            TableRow stockRow = new TableRow(getActivity());
                            stockRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                            stockRow.setPadding(5, 5, 5, 5);

                            for(String field : fields) {
                                TextView textView = new TextView(getActivity());
                                textView.setText(stockItem.get(field).toString());
                                textView.setPadding(0, 10, 10, 10);
                                stockRow.addView(textView);
                            }

                            stocks_TL.addView(stockRow);
                            loadingStocksDialog.dismiss();
                        }
                    } else {
                        while (stocks_TL.getChildCount() > 1)
                            stocks_TL.removeView(stocks_TL.getChildAt(stocks_TL.getChildCount() - 1));

                        TableRow stockRow = new TableRow(getActivity());
                        stockRow.setBackgroundColor(Color.parseColor("#3281a8"));
                        stockRow.setPadding(5, 5, 5, 5);

                        TextView textView = new TextView(getActivity());
                        textView.setText("No Stocks");
                        textView.setPadding(0, 10, 10, 10);
                        stockRow.addView(textView);

                        stocks_TL.addView(stockRow);
                        loadingStocksDialog.dismiss();
                    }

                } catch (JSONException e) {
                    loadingStocksDialog.dismiss();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingStocksDialog.dismiss();
                signOut();
                Toast.makeText(getActivity(), "Cannot reach cloud servers.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                params.put("dealer_id", orgID);
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                image_IV.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
