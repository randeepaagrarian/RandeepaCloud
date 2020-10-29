package com.randeepa.cloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.randeepa.cloud.api.API;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    private SharedPreferences userDetails;
    private Intent mainActivity;

    private RequestQueue mQueue;

    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        utils = new Utils();

        if(utils.isInternetAvailable(getApplicationContext()) == false) {
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Please connect to internet and sign in", Toast.LENGTH_SHORT).show();
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(main);
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        }

        userDetails = getSharedPreferences("user_details", MODE_PRIVATE);

        mQueue = Volley.newRequestQueue(this);

        validateAPIKey(userDetails.getString("username", null), userDetails.getString("api", null));

        View headerView = navigationView.getHeaderView(0);

        TextView welcomeGreeting = (TextView) headerView.findViewById(R.id.welcome_greeting);
        TextView email = (TextView) headerView.findViewById(R.id.email);
        ImageView profilePic = (ImageView) headerView.findViewById(R.id.profile_pic);

        welcomeGreeting.setText("Hello, " + userDetails.getString("name", null));
        email.setText(userDetails.getString("email", null));

        new DownloadImageTask(profilePic).execute(userDetails.getString("profile_pic", null));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                break;
            case R.id.nav_report_sale:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ReportSaleFragment()).commit();
                break;
            case R.id.nav_sales_banking:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SalesBankingFragment()).commit();
                break;
            case R.id.nav_spare_parts_banking:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SparePartsBankingFragment()).commit();
                break;
            case R.id.nav_organizational_visit:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationalVisitFragment()).commit();
                break;
            case R.id.nav_field_visit:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FieldVisitFragment()).commit();
                break;
            case R.id.nav_hirepurchase_banking:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HirePurchaseBankingFragment()).commit();
                break;
            case R.id.nav_claim_expense:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ClaimExpenseFragment()).commit();
                break;
            case R.id.nav_stock_review:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StockReviewFragment()).commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    private void validateAPIKey(final String username, final String api) {

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
                        Toast.makeText(getApplicationContext(), "Please sign in again", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY_RESPONSE_ERROR", String.valueOf(error));
                signOut();
                Toast.makeText(getApplicationContext(), "Cannot reach cloud servers.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("api", api);
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

        mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
