package com.digivahan.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.ComparisonSection;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SecVH> {

    public interface OnToggle {
        void onToggled(int pos, boolean expanded);
    }

    private final List<ComparisonSection> sections;
    private final OnToggle callback;

    public SectionAdapter(List<ComparisonSection> sections, OnToggle callback) {
        this.sections = sections;
        this.callback = callback;
    }

    @NonNull
    @Override public SecVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section, parent, false);
        return new SecVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SecVH h, int position) {
        ComparisonSection s = sections.get(position);
        h.tvTitle.setText(s.title);
        h.rvRows.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        h.rvRows.setAdapter(new RowAdapter(s.items));
        h.rvRows.setVisibility(s.expanded ? View.VISIBLE : View.GONE);
        h.ivToggle.setRotation(s.expanded ? 0f : 180f);

        h.header.setOnClickListener(v -> {
            s.expanded = !s.expanded;
            notifyItemChanged(position);
            if (callback != null) callback.onToggled(position, s.expanded);
        });
    }

    @Override public int getItemCount() { return sections.size(); }

    static class SecVH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivToggle;
        RecyclerView rvRows;
        View header;
        SecVH(@NonNull View v) {
            super(v);
            header = v.findViewById(R.id.rowHeader);
            tvTitle = v.findViewById(R.id.tvTitle);
            ivToggle = v.findViewById(R.id.ivToggle);
            rvRows = v.findViewById(R.id.rvRows);
        }
    }
}

