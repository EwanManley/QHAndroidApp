package com.example.qhapplicationv3;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

public class AddVendor extends AppCompatActivity {

    private static final String TAG = "AddVendor";
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String OFFICER_EMAIL = "officer@ipswich-city-council.qld.gov.au";
    private static final String OFFICER_PASSWORD = "Passw0rd!123";

    private final OkHttpClient http = new OkHttpClient();

    private EditText etLga, etName, etTrading, etPhone, etLicence, etExpiry, etReg, etDesc, etVehicle, etMake, etModel, etColour, etPrimary, etSerial, etOther1, etOther2, etStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page);

        etLga = findViewById(R.id.etLga);
        etName = findViewById(R.id.etName);
        etTrading = findViewById(R.id.etTrading);
        etPhone = findViewById(R.id.etPhone);
        etLicence = findViewById(R.id.etLicence);
        etExpiry = findViewById(R.id.etExpiry);
        etReg = findViewById(R.id.etReg);
        etDesc = findViewById(R.id.etDesc);
        etVehicle = findViewById(R.id.etVehicle);
        etMake = findViewById(R.id.etMake);
        etModel = findViewById(R.id.etModel);
        etColour = findViewById(R.id.etColour);
        etPrimary = findViewById(R.id.etPrimary);
        etSerial = findViewById(R.id.etSerial);
        etOther1 = findViewById(R.id.etOther1);
        etOther2 = findViewById(R.id.etOther2);
        etStatus = findViewById(R.id.etStatus);

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> beginCreate());
    }

    private void beginCreate() {
        String lga = nz(etLga.getText().toString());
        String name = nz(etName.getText().toString());
        String trading = nz(etTrading.getText().toString());
        String phone = nz(etPhone.getText().toString());
        String licence = nz(etLicence.getText().toString());
        String expiry = nz(etExpiry.getText().toString());
        String reg = nz(etReg.getText().toString());

        if (TextUtils.isEmpty(lga) || TextUtils.isEmpty(name) || TextUtils.isEmpty(trading)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(licence)
                || TextUtils.isEmpty(expiry) || TextUtils.isEmpty(reg)) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("[LGA Name]", lga);
            body.put("[* Name/s]", name);
            body.put("[* Trading name]", trading);

            String status = nz(etStatus.getText().toString());
            if (!status.isEmpty()) body.put("[Status]", status);

            body.put("[* Phone]", phone);
            body.put("[* Licence number]", licence);
            body.put("[* Expiry date]", expiry);
            body.put("[* Registration number]", reg);

            String desc = nz(etDesc.getText().toString());
            if (!desc.isEmpty()) body.put("[* Description of the food business]", desc);

            String vehicle = nz(etVehicle.getText().toString());
            if (!vehicle.isEmpty()) body.put("[Type of vehicle]", vehicle);

            String make = nz(etMake.getText().toString());
            if (!make.isEmpty()) body.put("[Make]", make);

            String model = nz(etModel.getText().toString());
            if (!model.isEmpty()) body.put("[Model]", model);

            String colour = nz(etColour.getText().toString());
            if (!colour.isEmpty()) body.put("[Colour]", colour);

            String primary = nz(etPrimary.getText().toString());
            if (!primary.isEmpty()) body.put("[Primary location of vending machine]", primary);

            String serial = nz(etSerial.getText().toString());
            if (!serial.isEmpty()) body.put("[* Serial number/ identification number/mark]", serial);

            String other1 = nz(etOther1.getText().toString());
            if (!other1.isEmpty()) body.put("[Other distinguishing features]", other1);

            String other2 = nz(etOther2.getText().toString());
            if (!other2.isEmpty()) body.put("[Other distinguishing features ]", other2);

        } catch (Exception e) {
            Toast.makeText(this, "Payload error", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "JSON payload: " + body.toString());
        obtainOfficerTokenAndCreate(body);
    }


    private void doCreate(String bearer, JSONObject body, boolean alreadyRetried) {
        Log.d(TAG, "Token being used for POST: " + bearer);
        Request req = new Request.Builder()
                .url(BASE + "/rest/v1/qh_register")
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + bearer)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")

                .post(RequestBody.create(body.toString(), JSON))
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(AddVendor.this,
                        "Create failed: " + safeMsg(e), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    Log.d(TAG, "HTTP code: " + response.code());
                    Log.d(TAG, "Response body: " + resp);
                    if ((response.code() == 401 || response.code() == 403) && !alreadyRetried) {
                        obtainOfficerTokenAndCreate(body);
                        return;
                    }
                    boolean ok = response.isSuccessful();
                    String msg = ok ? "Created"
                            : ("CREATE HTTP " + response.code() + " " + trim(resp, 400));
                    runOnUiThread(() -> {
                        Toast.makeText(AddVendor.this, msg, Toast.LENGTH_LONG).show();
                        if (ok) finish();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(AddVendor.this,
                            "Create parse error", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void obtainOfficerTokenAndCreate(JSONObject body) {
        try {
            JSONObject login = new JSONObject();
            login.put("email", OFFICER_EMAIL);
            login.put("password", OFFICER_PASSWORD);

            Request req = new Request.Builder()
                    .url(BASE + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", ANON)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(login.toString(), JSON))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddVendor.this,
                            "Auth failed: " + safeMsg(e), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String s = response.body() == null ? "" : response.body().string();
                        Log.d(TAG, "Officer token response: " + s);
                        String tok = new JSONObject(s).optString("access_token", null);
                        if (TextUtils.isEmpty(tok)) {
                            runOnUiThread(() -> Toast.makeText(AddVendor.this,
                                    "No token " + trim(s, 200), Toast.LENGTH_LONG).show());
                        } else {
                            UserAccount.get().setAccessToken(tok);
                            doCreate(tok, body, true);
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> Toast.makeText(AddVendor.this,
                                "Auth parse error", Toast.LENGTH_LONG).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Auth build error", Toast.LENGTH_LONG).show();
        }
    }

    private String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private String trim(String s, int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…");
    }

    private String safeMsg(Throwable t) {
        return t == null || t.getMessage() == null ? "" : t.getMessage();
    }
}
