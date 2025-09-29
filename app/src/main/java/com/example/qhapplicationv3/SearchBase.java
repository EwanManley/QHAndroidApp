package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchBase extends AppCompatActivity {
    protected abstract String getFieldKey();
    protected abstract String getScreenTitle();

    protected String role = "PUBLIC";
    protected String council = null;
    protected boolean canEditAll = false;

    protected final OkHttpClient client = new OkHttpClient();
    protected final List<JSONObject> all = new ArrayList<>();
    protected final List<JSONObject> filtered = new ArrayList<>();
    protected VendorList adapter;

    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";

    private static final String RPC_EXTERNAL = "/rest/v1/rpc/get_external_register";
    private static final String RPC_INTERNAL = "/rest/v1/rpc/get_internal_register";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int PAGE_LIMIT = 1000;
    private static final int PAGE_OFFSET = 0;

    private static final String OFFICER_EMAIL = "officer@ipswich-city-council.qld.gov.au";
    private static final String OFFICER_PASSWORD = "Passw0rd!123";
    private static final String AUTH_TOKEN_PATH = "/auth/v1/token?grant_type=password";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list_page);

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (UserAccount.get().getRole() != null) role = UserAccount.get().getRole();
        council = UserAccount.get().getCouncil();
        canEditAll = "QH".equalsIgnoreCase(role);

        Button add = findViewById(R.id.btnAdd);
        if (add != null) {
            if ("PUBLIC".equalsIgnoreCase(UserAccount.get().getRole())) {
                add.setVisibility(android.view.View.GONE);
            } else {
                add.setVisibility(android.view.View.VISIBLE);
                add.setOnClickListener(v -> startActivity(new Intent(this, AddVendor.class)));
            }
        }

        ((TextView) findViewById(R.id.titleField)).setText(getScreenTitle());

        RecyclerView rv = findViewById(R.id.recyclerVendors);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new VendorList(filtered, o -> {
            Intent d = new Intent(this, VendorDetails.class);
            d.putExtra("id", o.optString("id",""));
            d.putExtra("role", role);
            d.putExtra("lga", o.optString("[LGA Name]",""));
            d.putExtra("name", o.optString("[* Name/s]",""));
            d.putExtra("tradingName", o.optString("[* Trading name]",""));
            d.putExtra("status", o.optString("Status",""));
            d.putExtra("phone", o.optString("[* Phone]",""));
            d.putExtra("licence", o.optString("[* Licence number]",""));
            d.putExtra("expiry", o.optString("[* Expiry date]",""));
            d.putExtra("description", o.optString("[* Description of the food business]",""));
            d.putExtra("vehicle", o.optString("Type of vehicle",""));
            d.putExtra("make", o.optString("Make",""));
            d.putExtra("model", o.optString("Model",""));
            d.putExtra("colour", o.optString("Colour",""));
            d.putExtra("primaryLocation", o.optString("Primary location of vending machine",""));
            d.putExtra("serial", o.optString("[* Serial number/ identification number/mark]",""));
            d.putExtra("other1", o.optString("Other distinguishing features",""));
            d.putExtra("other2", o.optString("Other distinguishing features",""));
            startActivity(d);
        });
        rv.setAdapter(adapter);

        SearchView sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return true; }
        });

        if ("COUNCIL".equalsIgnoreCase(role) || "QH".equalsIgnoreCase(role)) {
            String token = UserAccount.get().getAccessToken();
            if (!TextUtils.isEmpty(token)) {
                Toast.makeText(this, "Using session token (internal)", Toast.LENGTH_SHORT).show();
                fetchInternalWithToken(token);
            } else {
                Toast.makeText(this, "Signing as fallback officer (internal)", Toast.LENGTH_SHORT).show();
                fetchInternalViaFallbackOfficer();
            }
        } else {
            Toast.makeText(this, "Loading public register", Toast.LENGTH_SHORT).show();
            fetchExternal();
        }
    }

    private void fetchExternal() {
        postRpc(RPC_EXTERNAL, null);
    }

    private void fetchInternalWithToken(String bearerToken) {
        postRpc(RPC_INTERNAL, bearerToken);
    }

    private void fetchInternalViaFallbackOfficer() {
        try {
            JSONObject body = new JSONObject();
            body.put("email", OFFICER_EMAIL);
            body.put("password", OFFICER_PASSWORD);

            Request req = new Request.Builder()
                    .url(BASE + AUTH_TOKEN_PATH)
                    .addHeader("apikey", ANON)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(SearchBase.this, "Auth error: " + (e.getMessage()==null?"":e.getMessage()), Toast.LENGTH_LONG).show();
                        fetchExternal();
                    });
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    String raw = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        String msg = "Auth HTTP " + response.code();
                        if (!TextUtils.isEmpty(raw)) msg += ": " + (raw.length()>160?raw.substring(0,160)+"…":raw);
                        String finalMsg = msg;
                        runOnUiThread(() -> { Toast.makeText(SearchBase.this, finalMsg, Toast.LENGTH_LONG).show(); fetchExternal(); });
                        return;
                    }
                    String token = null;
                    try { token = new JSONObject(raw).optString("access_token", null); } catch (Exception ignored) {}
                    if (TextUtils.isEmpty(token)) {
                        runOnUiThread(() -> { Toast.makeText(SearchBase.this, "No token", Toast.LENGTH_SHORT).show(); fetchExternal(); });
                    } else {
                        UserAccount.get().setAccessToken(token);
                        runOnUiThread(() -> Toast.makeText(SearchBase.this, "Internal register", Toast.LENGTH_SHORT).show());
                        postRpc(RPC_INTERNAL, token);
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Auth build error", Toast.LENGTH_SHORT).show();
            fetchExternal();
        }
    }

    private void postRpc(String rpcPath, String bearerToken) {
        JSONObject body = new JSONObject();
        try {
            body.put("p_limit", PAGE_LIMIT);
            body.put("p_offset", PAGE_OFFSET);
        } catch (Exception ignored) {}

        Request.Builder b = new Request.Builder()
                .url(BASE + rpcPath)
                .addHeader("apikey", ANON)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json");
        if (!TextUtils.isEmpty(bearerToken)) b.addHeader("Authorization", "Bearer " + bearerToken);

        Request req = b.post(RequestBody.create(body.toString(), JSON)).build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SearchBase.this, "Network error: " + (e.getMessage()==null?"":e.getMessage()), Toast.LENGTH_LONG).show();
                    filtered.clear();
                    adapter.notifyDataSetChanged();
                });
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    String msg = "HTTP " + response.code();
                    if (!TextUtils.isEmpty(raw)) msg += ": " + (raw.length() > 160 ? raw.substring(0,160) + "…" : raw);
                    String finalMsg = msg;
                    runOnUiThread(() -> Toast.makeText(SearchBase.this, finalMsg, Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    JSONArray arr = new JSONArray(raw);
                    all.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null) all.add(o);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(SearchBase.this, "Loaded " + all.size() + " rows", Toast.LENGTH_SHORT).show();
                        filter("");
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(SearchBase.this, "Parse error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    protected void filter(String q) {
        String s = q == null ? "" : q.trim().toLowerCase();
        filtered.clear();
        for (JSONObject o : all) {
            String key = getFieldKey();
            if (TextUtils.isEmpty(key)) {
                String t1 = o.optString("[* Trading name]","").toLowerCase();
                String t2 = o.optString("[* Registration number]","").toLowerCase();
                String t3 = o.optString("[* Licence number]","").toLowerCase();
                String t4 = o.optString("[* Name/s]","").toLowerCase();
                if (s.isEmpty() || t1.contains(s) || t2.contains(s) || t3.contains(s) || t4.contains(s)) {
                    filtered.add(o);
                }
            } else {
                String t = o.optString(key,"");
                if (s.isEmpty() || (t != null && t.toLowerCase().contains(s))) {
                    filtered.add(o);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
