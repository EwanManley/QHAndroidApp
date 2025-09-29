package com.example.qhapplicationv3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class EditDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String OFFICER_EMAIL = "officer@ipswich-city-council.qld.gov.au";
    private static final String OFFICER_PASSWORD = "Passw0rd!123";

    private String rowId;
    private EditText etTrading, etName, etPhone, etLicence, etReg, etExpiry, etStatus, etDesc, etVehicle, etMake, etModel, etColour, etPrimary, etSerial, etOther1, etOther2, etLga;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page);

        rowId = getIntent().getStringExtra("id");

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

        etLga.setText(nz(getIntent().getStringExtra("lga")));
        etName.setText(nz(getIntent().getStringExtra("name")));
        etTrading.setText(nz(getIntent().getStringExtra("tradingName")));
        etPhone.setText(nz(getIntent().getStringExtra("phone")));
        etLicence.setText(nz(getIntent().getStringExtra("licence")));
        etExpiry.setText(nz(getIntent().getStringExtra("expiry")));
        etReg.setText(nz(getIntent().getStringExtra("registration")));
        etDesc.setText(nz(getIntent().getStringExtra("description")));
        etVehicle.setText(nz(getIntent().getStringExtra("vehicle")));
        etMake.setText(nz(getIntent().getStringExtra("make")));
        etModel.setText(nz(getIntent().getStringExtra("model")));
        etColour.setText(nz(getIntent().getStringExtra("colour")));
        etPrimary.setText(nz(getIntent().getStringExtra("primaryLocation")));
        etSerial.setText(nz(getIntent().getStringExtra("serial")));
        etOther1.setText(nz(getIntent().getStringExtra("other1")));
        etOther2.setText(nz(getIntent().getStringExtra("other2")));
        etStatus.setText(nz(getIntent().getStringExtra("status")));

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        if (TextUtils.isEmpty(rowId)) {
            Toast.makeText(this, "Missing id", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject patch = new JSONObject();
        try {
            patch.put("[LGA Name]", nz(etLga.getText().toString()));
            patch.put("[* Name/s]", nz(etName.getText().toString()));
            patch.put("[* Trading name]", nz(etTrading.getText().toString()));
            String status = nz(etStatus.getText().toString());
            if (!status.isEmpty()) patch.put("[Status]", status);
            patch.put("[* Phone]", nz(etPhone.getText().toString()));
            patch.put("[* Licence number]", nz(etLicence.getText().toString()));
            patch.put("[* Expiry date]", nz(etExpiry.getText().toString()));
            patch.put("[* Registration number]", nz(etReg.getText().toString()));
            String desc = nz(etDesc.getText().toString());
            if (!desc.isEmpty()) patch.put("[* Description of the food business]", desc);
            String vehicle = nz(etVehicle.getText().toString());
            if (!vehicle.isEmpty()) patch.put("[Type of vehicle]", vehicle);
            String make = nz(etMake.getText().toString());
            if (!make.isEmpty()) patch.put("[Make]", make);
            String model = nz(etModel.getText().toString());
            if (!model.isEmpty()) patch.put("[Model]", model);
            String colour = nz(etColour.getText().toString());
            if (!colour.isEmpty()) patch.put("[Colour]", colour);
            String primary = nz(etPrimary.getText().toString());
            if (!primary.isEmpty()) patch.put("[Primary location of vending machine]", primary);
            String serial = nz(etSerial.getText().toString());
            if (!serial.isEmpty()) patch.put("[* Serial number/ identification number/mark]", serial);
            String other1 = nz(etOther1.getText().toString());
            if (!other1.isEmpty()) patch.put("[Other distinguishing features]", other1);
            String other2 = nz(etOther2.getText().toString());
            if (!other2.isEmpty()) patch.put("[Other distinguishing features ]", other2);
        } catch (Exception e) {
            Toast.makeText(this, "Error building payload", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = UserAccount.get().getAccessToken();
        if (TextUtils.isEmpty(token)) {
            obtainOfficerTokenAndPatch(patch);
        } else {
            doPatch(token, patch, false);
        }
    }

    private void doPatch(String bearer, JSONObject patch, boolean alreadyRetried) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/qh_register").newBuilder()
                .addQueryParameter("id", "eq." + rowId)
                .build();

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + bearer)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(RequestBody.create(patch.toString(), JSON))
                .build();

        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(EditDetails.this, "Save failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    if ((response.code() == 401 || response.code() == 403) && !alreadyRetried) {
                        obtainOfficerTokenAndPatch(patch);
                        return;
                    }
                    final boolean ok = response.isSuccessful();
                    final String msg = ok ? "Saved" : ("SAVE HTTP " + response.code() + (resp.isEmpty() ? "" : " " + trim(resp, 200)));
                    runOnUiThread(() -> {
                        Toast.makeText(EditDetails.this, msg, Toast.LENGTH_LONG).show();
                        if (ok) finish();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(EditDetails.this, "Save parse error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void obtainOfficerTokenAndPatch(JSONObject patch) {
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

            new OkHttpClient().newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(EditDetails.this, "Auth failed", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String s = response.body() == null ? "" : response.body().string();
                        String tok = new JSONObject(s).optString("access_token", null);
                        if (TextUtils.isEmpty(tok)) {
                            runOnUiThread(() -> Toast.makeText(EditDetails.this, "No token", Toast.LENGTH_SHORT).show());
                        } else {
                            UserAccount.get().setAccessToken(tok);
                            doPatch(tok, patch, true);
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> Toast.makeText(EditDetails.this, "Auth parse error", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Auth build error", Toast.LENGTH_SHORT).show();
        }
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private String trim(String s, int n) { return s.length() <= n ? s : s.substring(0, n) + "…"; }
}
