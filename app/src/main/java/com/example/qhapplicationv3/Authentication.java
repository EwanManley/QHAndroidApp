package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class Authentication extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword;
    private EditText etRegEmail, etRegPassword, etRegConfirm;
    private RadioGroup rgRole;
    private RadioButton rbCouncilMember;
    private Spinner spinnerCouncil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_page);

        UserAccount.get().loadSavedSession(this);

        TextView tabLogin = findViewById(R.id.tabLogin);
        TextView tabRegister = findViewById(R.id.tabRegister);
        View indicatorLogin = findViewById(R.id.indicatorLogin);
        View indicatorRegister = findViewById(R.id.indicatorRegister);
        LinearLayout loginForm = findViewById(R.id.loginForm);
        LinearLayout registerForm = findViewById(R.id.registerForm);

        tabLogin.setOnClickListener(v -> {
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
            indicatorLogin.setBackgroundColor(0xFF2196F3);
            indicatorRegister.setBackgroundColor(0xFFE0E0E0);
        });
        tabRegister.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            indicatorLogin.setBackgroundColor(0xFFE0E0E0);
            indicatorRegister.setBackgroundColor(0xFF2196F3);
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        Button btnPublicLogin = findViewById(R.id.btnPublicLogin);
        btnPublicLogin.setOnClickListener(v -> {
            goToVendors("public@guest", "PUBLIC", null, null);
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirm = findViewById(R.id.etRegConfirm);
        rgRole = findViewById(R.id.rgRole);
        rbCouncilMember = findViewById(R.id.rbCouncilMember);
        TextView councilLabel = findViewById(R.id.councilLabel);
        spinnerCouncil = findViewById(R.id.spinnerCouncil);

        List<String> councils = Arrays.asList(
                "Aurukun Shire Council",
                "Balonne Shire Council",
                "Banana Shire Council",
                "Barcaldine Regional Council",
                "Barcoo Shire Council",
                "Blackall-Tambo Regional Council",
                "Boulia Shire Council",
                "Bulloo Shire Council",
                "Bundaberg Regional Council",
                "Burdekin Shire Council",
                "Burke Shire Council",
                "Cairns Regional Council",
                "Carpentaria Shire Council",
                "Cassowary Coast Regional Council",
                "Central Highlands Regional Council",
                "Charters Towers Regional Council",
                "Cherbourg Aboriginal Shire Council",
                "Cloncurry Shire Council",
                "Cook Shire Council",
                "Croydon Shire Council",
                "Diamantina Shire Council",
                "Doomadgee Aboriginal Shire Council",
                "Douglas Shire Council",
                "Etheridge Shire Council",
                "Flinders Shire Council",
                "Fraser Coast Regional Council",
                "Gladstone Regional Council",
                "Gold Coast City Council",
                "Goondiwindi Regional Council",
                "Gympie Regional Council",
                "Hinchinbrook Shire Council",
                "Hope Vale Aboriginal Shire Council",
                "Ipswich City Council",
                "Isaac Regional Council",
                "Kowanyama Aboriginal Shire Council",
                "Livingstone Shire Council",
                "Lockhart River Aboriginal Shire Council",
                "Lockyer Valley Regional Council",
                "Logan City Council",
                "Longreach Regional Council",
                "Mackay Regional Council",
                "Mapoon Aboriginal Shire Council",
                "Maranoa Regional Council",
                "Mareeba Shire Council",
                "McKinlay Shire Council",
                "Moreton Bay City Council",
                "Mornington Shire Council",
                "Mount Isa City Council",
                "Murweh Shire Council",
                "Napranum Aboriginal Shire Council",
                "Noosa Shire Council",
                "North Burnett Regional Council",
                "Northern Peninsual Area Regional Council",
                "Palm Island Aboriginal Shire Council",
                "Paroo Shire Council",
                "Pormpuraaw Aboriginal Shire Council",
                "Quilpie Shire Council",
                "Redland City Council",
                "Richmond Shire Council",
                "Rockhampton Regional Council",
                "Scenic Rim Regional Council",
                "Somerset Regional Council",
                "South Burnett Regional Council",
                "Southern Downs Regional Council",
                "Sunshine Coast Regional Council",
                "Tablelands Regional Council",
                "Toowoomba Regional Council",
                "Torres Shire Council",
                "Torres Strait Island Regional Council",
                "Townsville City Council",
                "Weipa Town",
                "Western Downs Regional Council",
                "Whitsunday Regional Council",
                "Winton Shire Council",
                "Woorabinda Aboriginal Shire Council",
                "Wujal Wujal Aboriginal Shire Council",
                "Yarrabah Aboriginal Shire Council"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, councils);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCouncil.setAdapter(adapter);
        if (adapter.getCount() > 0) spinnerCouncil.setSelection(0);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean council = checkedId == R.id.rbCouncilMember;
            councilLabel.setVisibility(council ? View.VISIBLE : View.GONE);
            spinnerCouncil.setVisibility(council ? View.VISIBLE : View.GONE);
            if (council && spinnerCouncil.getSelectedItemPosition() == AdapterView.INVALID_POSITION && adapter.getCount() > 0) {
                spinnerCouncil.setSelection(0);
            }
        });

        btnRegister.setOnClickListener(v -> onRegister());
        btnLogin.setOnClickListener(v -> onLogin());
    }

    private void onRegister() {
        String email = txt(etRegEmail);
        String pass = txt(etRegPassword);
        String conf = txt(etRegConfirm);
        boolean councilRole = rbCouncilMember != null && rbCouncilMember.isChecked();
        String councilSel = null;

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(conf)) { toast("Fill all fields"); return; }
        if (!pass.equals(conf)) { toast("Passwords do not match"); return; }
        if (councilRole) {
            int idx = spinnerCouncil == null ? AdapterView.INVALID_POSITION : spinnerCouncil.getSelectedItemPosition();
            if (idx == AdapterView.INVALID_POSITION) { toast("Select a council"); return; }
            Object sel = spinnerCouncil.getSelectedItem();
            if (sel == null || TextUtils.isEmpty(sel.toString())) { toast("Select a council"); return; }
            councilSel = sel.toString();
        }

        final String selectedCouncil = councilSel;
        String role = councilRole ? "COUNCIL" : "QH";

        toast("Registering…");
        UserAccount.register(this, email, pass, role, selectedCouncil, (ok, message, profile) -> {
            runOnUiThread(() -> {
                if (!ok) {
                    toast(message == null ? "Registration failed" : message);
                    return;
                }
                toast("Registered");
                goToVendors(email, role, selectedCouncil, UserAccount.get().getAccessToken());
            });
        });
    }

    private void onLogin() {
        String email = txt(etLoginEmail);
        String pass = txt(etLoginPassword);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) { toast("Enter email and password"); return; }

        toast("Signing in…");
        UserAccount.login(this, email, pass, (ok, message, profile) -> {
            runOnUiThread(() -> {
                if (!ok) {
                    toast(message == null ? "Login failed" : message);
                    return;
                }
                String role = UserAccount.get().getRole();
                String council = UserAccount.get().getCouncil();
                goToVendors(email, role, council, UserAccount.get().getAccessToken());
            });
        });
    }

    private void goToVendors(String email, String role, String council, String accessToken) {
        UserAccount.get().setEmail(email);
        UserAccount.get().setRole(role);
        UserAccount.get().setCouncil(council);
        UserAccount.get().setAccessToken(accessToken);
        UserAccount.get().saveSession(this);
        Intent i = new Intent(this, SearchFilters.class);
        startActivity(i);
        finish();
    }

    private static String txt(EditText e) { return e == null ? "" : e.getText().toString().trim(); }
    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
