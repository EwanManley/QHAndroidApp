package com.example.qhapplicationv3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class EditDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String OFFICER_EMAIL = "officer@ipswich-city-council.qld.gov.au";
    private static final String OFFICER_PASSWORD = "Passw0rd!123";

    private final OkHttpClient http = new OkHttpClient();

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
        Button btnSave   = findViewById(R.id.btnSave);
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
            putReq(patch, "[LGA Name]", etLga);
            putReq(patch, "[* Name/s]", etName);
            putReq(patch, "[* Trading name]", etTrading);
            String status = nz(etStatus.getText().toString());
            if (!status.isEmpty()) patch.put("[Status]", status);
            putReq(patch, "[* Phone]", etPhone);
            putReq(patch, "[* Licence number]", etLicence);
            putReq(patch, "[* Expiry date]", etExpiry);
            putReq(patch, "[* Registration number]", etReg);
            putOpt(patch, "[* Description of the food business]", etDesc);
            putOpt(patch, "[Type of vehicle]", etVehicle);
            putOpt(patch, "[Make]", etMake);
            putOpt(patch, "[Model]", etModel);
            putOpt(patch, "[Colour]", etColour);
            putOpt(patch, "[Primary location of vending machine]", etPrimary);
            putOpt(patch, "[* Serial number/ identification number/mark]", etSerial);
            putOpt(patch, "[Other distinguishing features]", etOther1);
            putOpt(patch, "[Other distinguishing features ]", etOther2);
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
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(RequestBody.create(patch.toString(), JSON))
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(EditDetails.this,
                        "Save failed: " + safeMsg(e), Toast.LENGTH_LONG).show());
            }
            @Override public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    if ((response.code() == 401 || response.code() == 403) && !alreadyRetried) {
                        obtainOfficerTokenAndPatch(patch);
                        return;
                    }
                    if (response.isSuccessful()) {
                        boolean hasBody = !resp.trim().isEmpty();
                        boolean updated = hasBody && looksLikeUpdated(resp);
                        if (!hasBody) {
                            String cr = response.header("Content-Range", "");
                            if (cr != null && (cr.endsWith("/0") || cr.contains("*/0"))) {
                                runOnUiThread(() -> Toast.makeText(EditDetails.this,
                                        "No row updated", Toast.LENGTH_LONG).show());
                                return;
                            }
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(EditDetails.this,
                                    updated ? "Saved" : "Saved (check record)",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(EditDetails.this,
                                "SAVE HTTP " + response.code() + " " + trim(resp, 800),
                                Toast.LENGTH_LONG).show());
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(EditDetails.this,
                            "Save parse error", Toast.LENGTH_LONG).show());
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
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(EditDetails.this,
                            "Auth failed: " + safeMsg(e), Toast.LENGTH_LONG).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String s = response.body() == null ? "" : response.body().string();
                        String tok = new JSONObject(s).optString("access_token", null);
                        if (TextUtils.isEmpty(tok)) {
                            runOnUiThread(() -> Toast.makeText(EditDetails.this,
                                    "No token " + trim(s, 200),
                                    Toast.LENGTH_LONG).show());
                        } else {
                            UserAccount.get().setAccessToken(tok);
                            doPatch(tok, patch, true);
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> Toast.makeText(EditDetails.this,
                                "Auth parse error", Toast.LENGTH_LONG).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Auth build error", Toast.LENGTH_SHORT).show();
        }
    }

    private void putReq(JSONObject o, String key, EditText src) throws Exception {
        o.put(key, nz(src.getText().toString()));
    }
    private void putOpt(JSONObject o, String key, EditText src) throws Exception {
        String v = nz(src.getText().toString());
        if (!v.isEmpty()) o.put(key, v);
    }

    private boolean looksLikeUpdated(String body) {
        try { return new JSONArray(body).length() > 0; }
        catch (Exception ignore) {
            return body.trim().startsWith("{") || body.trim().startsWith("[");
        }
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private String trim(String s, int n) { return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…"); }
    private String safeMsg(Throwable t) { return t == null || t.getMessage() == null ? "" : t.getMessage(); }
}
