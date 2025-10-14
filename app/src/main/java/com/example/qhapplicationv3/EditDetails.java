package com.example.qhapplicationv3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class EditDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient();

    private String rowId;
    private EditText etTrading, etName, etPhone, etLicence, etReg, etExpiry, etStatus, etDesc, etVehicle, etMake, etModel, etColour, etPrimary, etSerial, etOther1, etLga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roleRaw = String.valueOf(UserAccount.get().getRole());
        String role = roleRaw == null ? "" : roleRaw.trim().toUpperCase();

        boolean isQH = role.equals("QH") || role.equals("QLD") || role.equals("QLD_HEALTH") || role.equals("QUEENSLAND_HEALTH");
        boolean isCouncil = role.equals("COUNCIL");

        if (isQH) {
            setContentView(R.layout.edit_qh_page);
        } else {
            setContentView(R.layout.edit_page);
        }

        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        TextView tvLgaContext = findViewById(R.id.tvLgaContext);

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
        etStatus = findViewById(R.id.etStatus);
        android.widget.Spinner spLga = findViewById(R.id.spLga);

        String council = UserAccount.get().getCouncil();

        if (tvFormTitle != null) tvFormTitle.setText("Edit vendor");
        if (tvLgaContext != null) {
            tvLgaContext.setText(isQH ? "Editing in: Choose council" : "Editing in: " + CouncilLookup.toDisplay(council));
        }

        if (etLga != null) etLga.setText(nz(getIntent().getStringExtra("lga")));
        if (etName != null) etName.setText(nz(getIntent().getStringExtra("name")));
        if (etTrading != null) etTrading.setText(nz(getIntent().getStringExtra("tradingName")));
        if (etPhone != null) etPhone.setText(nz(getIntent().getStringExtra("phone")));
        if (etLicence != null) etLicence.setText(nz(getIntent().getStringExtra("licence")));
        if (etExpiry != null) etExpiry.setText(nz(getIntent().getStringExtra("expiry")));
        if (etReg != null) etReg.setText(nz(getIntent().getStringExtra("registration")));
        if (etDesc != null) etDesc.setText(nz(getIntent().getStringExtra("description")));
        if (etVehicle != null) etVehicle.setText(nz(getIntent().getStringExtra("vehicle")));
        if (etMake != null) etMake.setText(nz(getIntent().getStringExtra("make")));
        if (etModel != null) etModel.setText(nz(getIntent().getStringExtra("model")));
        if (etColour != null) etColour.setText(nz(getIntent().getStringExtra("colour")));
        if (etPrimary != null) etPrimary.setText(nz(getIntent().getStringExtra("primaryLocation")));
        if (etSerial != null) etSerial.setText(nz(getIntent().getStringExtra("serial")));
        if (etOther1 != null) etOther1.setText(nz(getIntent().getStringExtra("other1")));
        if (etStatus != null) etStatus.setText(nz(getIntent().getStringExtra("status")));

        if (isCouncil && !TextUtils.isEmpty(council)) {
            if (etLga != null) {
                etLga.setText(CouncilLookup.toDisplay(council));
                etLga.setFocusable(false);
                etLga.setEnabled(false);
                etLga.setClickable(false);
            }
            if (spLga != null) spLga.setVisibility(View.GONE);
        } else if (isQH) {
            if (spLga != null) {
                java.util.List<String> councils = CouncilLookup.all();
                android.widget.ArrayAdapter<String> a =
                        new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, councils);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spLga.setAdapter(a);
                spLga.setVisibility(View.VISIBLE);
                if (etLga != null) etLga.setVisibility(View.GONE);
                spLga.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                        if (etLga != null) etLga.setText(councils.get(pos));
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                });
            }
        }

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
    }

    private void showCouncilPicker() {
        java.util.List<String> councils = CouncilLookup.all();
        String[] arr = councils.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Select council")
                .setItems(arr, (d, which) -> etLga.setText(arr[which]))
                .show();
    }

    private void save() {
        if (TextUtils.isEmpty(rowId)) {
            Toast.makeText(this, "Missing id", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        String bearer = UserAccount.get().getAccessToken();

        if (TextUtils.isEmpty(bearer)) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_LONG).show();
            return;
        }
        if ("PUBLIC".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Not permitted", Toast.LENGTH_LONG).show();
            return;
        }
        if ("COUNCIL".equalsIgnoreCase(role) && !TextUtils.isEmpty(council)) {
            String currentLga = nz(etLga.getText().toString());
            boolean okExact = council.equals(currentLga);
            boolean okSlugged = council.equalsIgnoreCase(slug(currentLga));
            boolean okDisplay = currentLga.equalsIgnoreCase(council.replace("-", " "));
            Log.d("EditDetails", "council(stored)=" + council + " currentLga=" + currentLga + " exact=" + okExact + " slugged=" + okSlugged + " display=" + okDisplay);
            if (!(okExact || okSlugged || okDisplay)) {
                Toast.makeText(this, "LGA must be " + council, Toast.LENGTH_LONG).show();
                return;
            }
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
        } catch (Exception e) {
            Toast.makeText(this, "Error building payload", Toast.LENGTH_SHORT).show();
            return;
        }

        doPatch(bearer, patch);
    }

    private void doPatch(String bearer, JSONObject patch) {
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
                runOnUiThread(() -> Toast.makeText(EditDetails.this, "Save failed: " + safeMsg(e), Toast.LENGTH_LONG).show());
            }
            @Override public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(EditDetails.this, "SAVE HTTP " + response.code() + " " + trim(resp, 800), Toast.LENGTH_LONG).show());
                        return;
                    }
                    boolean hasBody = !resp.trim().isEmpty();
                    boolean updated = hasBody && looksLikeUpdated(resp);
                    if (!hasBody) {
                        String cr = response.header("Content-Range", "");
                        if (cr != null && (cr.endsWith("/0") || cr.contains("*/0"))) {
                            runOnUiThread(() -> Toast.makeText(EditDetails.this, "No row updated", Toast.LENGTH_LONG).show());
                            return;
                        }
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(EditDetails.this, updated ? "Saved" : "Saved (check record)", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(EditDetails.this, "Save parse error", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void putReq(JSONObject o, String key, EditText src) throws Exception { o.put(key, nz(src.getText().toString())); }
    private void putOpt(JSONObject o, String key, EditText src) throws Exception { String v = nz(src.getText().toString()); if (!v.isEmpty()) o.put(key, v); }

    private boolean looksLikeUpdated(String body) {
        try { return new JSONArray(body).length() > 0; }
        catch (Exception ignore) { return body.trim().startsWith("{") || body.trim().startsWith("["); }
    }

    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private String trim(String s, int n) { return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…"); }
    private String safeMsg(Throwable t) { return t == null || t.getMessage() == null ? "" : t.getMessage(); }
}
