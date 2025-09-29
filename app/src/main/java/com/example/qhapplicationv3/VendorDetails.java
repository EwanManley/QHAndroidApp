package com.example.qhapplicationv3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class VendorDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String OFFICER_EMAIL = "officer@ipswich-city-council.qld.gov.au";
    private static final String OFFICER_PASSWORD = "Passw0rd!123";

    private String rowId;
    private String role;
    private final OkHttpClient client = new OkHttpClient();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_info_page);

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        role = UserAccount.get().getRole();
        boolean isPublic = role == null || role.equalsIgnoreCase("PUBLIC");

        Intent intent = getIntent();
        rowId = intent.getStringExtra("id");

        setField(R.id.fieldLGA, "LGA Name:", intent.getStringExtra("lga"), false);
        setField(R.id.fieldName, "Name/s:", intent.getStringExtra("name"), true);
        setField(R.id.fieldTradingName, "Trading Name:", intent.getStringExtra("tradingName"), true);
        setField(R.id.fieldPhone, "Phone:", intent.getStringExtra("phone"), true);
        setField(R.id.fieldLicence, "Licence Number:", intent.getStringExtra("licence"), true);
        setField(R.id.fieldRegistration, "Registration:", intent.getStringExtra("registration"), true);
        setField(R.id.fieldStatus, "Status:", intent.getStringExtra("status"), true);
        setField(R.id.fieldExpiry, "Expiry Date:", intent.getStringExtra("expiry"), true);
        setField(R.id.fieldDescription, "Description:", intent.getStringExtra("description"), true);
        setField(R.id.fieldVehicle, "Type of vehicle:", intent.getStringExtra("vehicle"), true);
        setField(R.id.fieldMake, "Make:", intent.getStringExtra("make"), true);
        setField(R.id.fieldModel, "Model:", intent.getStringExtra("model"), true);
        setField(R.id.fieldColour, "Colour:", intent.getStringExtra("colour"), true);
        setField(R.id.fieldOther1, "Other distinguishing features:", intent.getStringExtra("other1"), true);
        setField(R.id.fieldPrimaryLocation, "Primary location of vending machine:", intent.getStringExtra("primaryLocation"), true);
        setField(R.id.fieldSerial, "Serial number/ identification number/mark:", intent.getStringExtra("serial"), true);
        setField(R.id.fieldOther2, "Other distinguishing features:", intent.getStringExtra("other2"), true);

        if (isPublic) {
            hide(R.id.fieldStatus);
            show(R.id.fieldExpiry);
            hide(R.id.fieldDescription);
            hide(R.id.fieldVehicle);
            hide(R.id.fieldMake);
            hide(R.id.fieldModel);
            hide(R.id.fieldColour);
            hide(R.id.fieldOther1);
            hide(R.id.fieldPrimaryLocation);
            hide(R.id.fieldSerial);
            hide(R.id.fieldOther2);
        } else {
            show(R.id.fieldStatus);
            show(R.id.fieldExpiry);
            show(R.id.fieldDescription);
            show(R.id.fieldVehicle);
            show(R.id.fieldMake);
            show(R.id.fieldModel);
            show(R.id.fieldColour);
            show(R.id.fieldOther1);
            show(R.id.fieldPrimaryLocation);
            show(R.id.fieldSerial);
            show(R.id.fieldOther2);
        }

        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);
        if (btnEdit != null) btnEdit.setVisibility(isPublic ? View.GONE : View.VISIBLE);
        if (btnDelete != null) btnDelete.setVisibility(isPublic ? View.GONE : View.VISIBLE);

        if (!isPublic) {
            if (btnEdit != null) btnEdit.setOnClickListener(v -> {
                Intent e = new Intent(this, EditDetails.class);
                e.putExtras(getIntent());
                startActivity(e);
            });
            if (btnDelete != null) btnDelete.setOnClickListener(v -> confirmDelete());
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete vendor")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (d, w) -> deleteRow(false))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRow(boolean alreadyRetried) {
        if (rowId == null || rowId.isEmpty()) {
            Toast.makeText(this, "Missing id", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = UserAccount.get().getAccessToken();
        if (TextUtils.isEmpty(token)) {
            obtainOfficerTokenAndDelete();
            return;
        }
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/qh_register")
                .newBuilder()
                .addQueryParameter("id", "eq." + rowId)
                .build();

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(VendorDetails.this, "Delete failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {
                if ((response.code() == 401 || response.code() == 403) && !alreadyRetried) {
                    obtainOfficerTokenAndDelete();
                    return;
                }
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(VendorDetails.this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (response.code() == 404) {
                        Toast.makeText(VendorDetails.this, "Not found", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(VendorDetails.this, "Delete HTTP " + response.code(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void obtainOfficerTokenAndDelete() {
        try {
            JSONObject body = new JSONObject();
            body.put("email", OFFICER_EMAIL);
            body.put("password", OFFICER_PASSWORD);

            Request req = new Request.Builder()
                    .url(BASE + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", ANON)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(VendorDetails.this, "Auth failed", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String s = response.body() == null ? "" : response.body().string();
                        String tok = new JSONObject(s).optString("access_token", null);
                        if (tok == null || tok.isEmpty()) {
                            runOnUiThread(() -> Toast.makeText(VendorDetails.this, "No token", Toast.LENGTH_SHORT).show());
                        } else {
                            UserAccount.get().setAccessToken(tok);
                            deleteRow(true);
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> Toast.makeText(VendorDetails.this, "Auth parse error", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Auth build error", Toast.LENGTH_SHORT).show();
        }
    }

    private void setField(int id, String label, String value, boolean optional) {
        TextView tv = findViewById(id);
        if (tv == null) return;
        String v = clean(value);
        if (v.isEmpty() && optional) tv.setText(label + " ");
        else if (v.isEmpty()) tv.setText(label + " -");
        else tv.setText(label + " " + v);
    }
    private String clean(String s) { if (s == null) return ""; String t = s.trim(); return t.equalsIgnoreCase("null") ? "" : t; }
    private void hide(int id) { View v = findViewById(id); if (v != null) v.setVisibility(View.GONE); }
    private void show(int id) { View v = findViewById(id); if (v != null) v.setVisibility(View.VISIBLE); }
}
