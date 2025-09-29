package com.example.qhapplicationv3;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class UserAccount {

    private static UserAccount instance;
    private UserAccount() {}
    public static UserAccount get() {
        if (instance == null) instance = new UserAccount();
        return instance;
    }

    private String email;
    private String role;
    private String council;
    private String accessToken;

    private static final Map<String, JSONObject> accounts = new HashMap<>();

    public static boolean register(Context ctx, String email, String password, String role, String council) {
        if (accounts.containsKey(email)) return false;

        JSONObject obj = new JSONObject();
        try {
            obj.put("email", email);
            obj.put("password", password);
            obj.put("role", role);
            if (council != null) obj.put("council", council);
        } catch (Exception ignored) {}

        accounts.put(email, obj);

        saveAccount(ctx, email, obj);
        return true;
    }

    public static JSONObject login(Context ctx, String email, String password) {
        JSONObject obj = accounts.get(email);

        if (obj == null) {
            obj = loadAccount(ctx, email);
            if (obj != null) accounts.put(email, obj);
        }

        if (obj == null) return null;
        if (!password.equals(obj.optString("password"))) return null;

        get().email = email;
        get().role = obj.optString("role");
        get().council = obj.optString("council", null);
        return obj;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCouncil() { return council; }
    public void setCouncil(String council) { this.council = council; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    private static void saveAccount(Context ctx, String email, JSONObject obj) {
        SharedPreferences prefs = ctx.getSharedPreferences("accounts", Context.MODE_PRIVATE);
        prefs.edit().putString(email, obj.toString()).apply();
    }

    private static JSONObject loadAccount(Context ctx, String email) {
        SharedPreferences prefs = ctx.getSharedPreferences("accounts", Context.MODE_PRIVATE);
        String raw = prefs.getString(email, null);
        if (raw == null) return null;
        try {
            return new JSONObject(raw);
        } catch (Exception e) {
            return null;
        }
    }
}
