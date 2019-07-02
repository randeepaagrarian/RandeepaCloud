package com.randeepa.cloud;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SparePartsBankingFragment extends Fragment implements View.OnClickListener {

    private Utils utils;

    private EditText date_ET;
    private EditText bank_ET;
    private EditText branch_ET;
    private EditText amount_ET;
    private EditText invoice_number_ET;

    private ImageView image_IV;
    private int REQUEST_CODE = 1;

    private Button report_spare_parts_banking_BT;

    private AlertDialog reportSparePartsBankingStatusAlert;

    private RequestQueue mQueue;

    private SharedPreferences userDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View sparePartsBanking = inflater.inflate(R.layout.fragment_spare_parts_banking, container, false);

        utils = new Utils();

        mQueue = Volley.newRequestQueue(getActivity());

        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);

        validateAPIKey();

        date_ET = sparePartsBanking.findViewById(R.id.date_ET);
        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());
        date_ET.setOnClickListener(this);

        image_IV = sparePartsBanking.findViewById(R.id.image_IV);
        image_IV.setOnClickListener(this);

        bank_ET = sparePartsBanking.findViewById(R.id.bank_ET);
        branch_ET = sparePartsBanking.findViewById(R.id.branch_ET);
        amount_ET = sparePartsBanking.findViewById(R.id.amount_ET);
        invoice_number_ET = sparePartsBanking.findViewById(R.id.invoice_number_ET);

        report_spare_parts_banking_BT = sparePartsBanking.findViewById(R.id.report_spare_parts_banking_BT);
        report_spare_parts_banking_BT.setOnClickListener(this);

        return sparePartsBanking;
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
            case R.id.image_IV:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
                break;
            case R.id.report_spare_parts_banking_BT:

                if (image_IV.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.upload).getConstantState()) {
                    Toast.makeText(getActivity(), "Select the image", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(date_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(bank_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the bank", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(branch_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the branch", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(amount_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(invoice_number_ET.getText().toString())) {
                    Toast.makeText(getActivity(), "Enter the invoice number", Toast.LENGTH_SHORT).show();
                    return;
                }

                reportSparePartsBanking();

                break;
        }
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

    private void reportSparePartsBanking() {

        reportSparePartsBankingStatusAlert = new ProgressDialog(getActivity());
        reportSparePartsBankingStatusAlert.setTitle("Reporting Spare Parts Banking...");
        reportSparePartsBankingStatusAlert.setMessage("Please wait while the spare parts banking is being reported.");
        reportSparePartsBankingStatusAlert.setCancelable(false);
        reportSparePartsBankingStatusAlert.show();

        String url = "https://www.randeepa.cloud/android-api6/spare_parts_banking";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("SPARE_PARTS_RES", response);

                reportSparePartsBankingStatusAlert.dismiss();
                try {
                    JSONObject reportSparePartsBankingRes = new JSONObject(response);
                    boolean reportSparePartsBankingSuccess = Boolean.parseBoolean(reportSparePartsBankingRes.getString("status"));
                    String reportSparePartsBankingMessage = reportSparePartsBankingRes.getString("message");

                    if(reportSparePartsBankingSuccess) {

                        reportSparePartsBankingStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportSparePartsBankingStatusAlert.setTitle("Spare parts banking reported successfully");
                        reportSparePartsBankingStatusAlert.setMessage(reportSparePartsBankingMessage);
                        reportSparePartsBankingStatusAlert.setIcon(getResources().getDrawable(R.drawable.success_icon));
                        reportSparePartsBankingStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportSparePartsBankingStatusAlert.show();

                    } else {
                        reportSparePartsBankingStatusAlert = new AlertDialog.Builder(getActivity()).create();
                        reportSparePartsBankingStatusAlert.setTitle("Failed to report spare parts banking");
                        reportSparePartsBankingStatusAlert.setMessage(reportSparePartsBankingMessage);
                        reportSparePartsBankingStatusAlert.setIcon(getResources().getDrawable(R.drawable.failure_icon));
                        reportSparePartsBankingStatusAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        reportSparePartsBankingStatusAlert.show();
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
                params.put("image", imageB64);
                params.put("username", userDetails.getString("username", null));
                params.put("api", userDetails.getString("api", null));
                params.put("region", userDetails.getString("region", null));
                params.put("territory", userDetails.getString("territory", null));
                params.put("bank", bank_ET.getText().toString());
                params.put("branch", branch_ET.getText().toString());
                params.put("amount", amount_ET.getText().toString());
                params.put("date", date_ET.getText().toString());
                params.put("invoice_number", invoice_number_ET.getText().toString());
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
