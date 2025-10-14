package com.example.qhapplicationv3;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.List;

public class VendorList extends RecyclerView.Adapter<VendorList.VH> {

    public interface OnRowClick { void onClick(JSONObject obj); }

    private final List<JSONObject> items;
    private final OnRowClick onRowClick;

    public VendorList(List<JSONObject> items, OnRowClick onRowClick) {
        this.items = items;
        this.onRowClick = onRowClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_page, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        JSONObject o = items.get(position);
        String title = o.optString("[* Trading name]", "-");
        String reg = o.optString("[* Registration number]", "-");
        String lga = o.optString("[LGA Name]", "-");
        String id = String.valueOf(o.opt("id"));

        h.title.setText(title);
        h.subtitle.setText(reg + " • " + lga);
        h.itemView.setOnClickListener(v -> onRowClick.onClick(o));

        String roleRaw = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        String role = roleRaw == null ? "" : roleRaw.trim();

        boolean canDelete;
        if (role.isEmpty() || role.equalsIgnoreCase("PUBLIC")) {
            canDelete = false;
        } else if (role.equalsIgnoreCase("QH")) {
            canDelete = true;
        } else if (role.equalsIgnoreCase("COUNCIL") && !TextUtils.isEmpty(council)) {
            String rec = lga == null ? "" : lga;
            boolean okExact = council.equals(rec);
            boolean okSlug = council.equalsIgnoreCase(slug(rec));
            boolean okDisplay = rec.equalsIgnoreCase(council.replace("-", " "));
            canDelete = okExact || okSlug || okDisplay;
        } else {
            canDelete = false;
        }

        if (h.btnDelete != null) {
            h.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            h.btnDelete.setOnClickListener(v -> {
                if (TextUtils.isEmpty(id) || "null".equalsIgnoreCase(id)) {
                    Toast.makeText(v.getContext(), "Missing id", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete vendor")
                        .setMessage("Delete \"" + title + "\"?")
                        .setPositiveButton("Delete", (d, w) -> {
                            VendorDetails.deleteVendor(v.getContext(), id, lga, () -> {
                                int p = h.getAdapterPosition();
                                if (p != RecyclerView.NO_POSITION) {
                                    items.remove(p);
                                    notifyItemRemoved(p);
                                    notifyItemRangeChanged(p, getItemCount() - p);
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Button btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
            btnDelete = itemView.findViewById(R.id.btnRowDelete);
        }
    }

    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }
}
