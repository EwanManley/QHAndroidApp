package com.example.qhapplicationv3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
        h.title.setText(o.optString("[* Trading name]","-"));
        String sub = o.optString("[* Registration number]","-");
        String lga = o.optString("[LGA Name]","-");
        h.subtitle.setText(sub + " • " + lga);
        h.itemView.setOnClickListener(v -> onRowClick.onClick(o));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
        }
    }
}
