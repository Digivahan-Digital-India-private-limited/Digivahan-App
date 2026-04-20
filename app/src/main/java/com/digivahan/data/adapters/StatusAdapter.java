package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;

import java.util.ArrayList;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private ArrayList<String> statusList;
    private int selectedItem = 0; // Default no selection
    private OnStatusClickListener listener;

    public interface OnStatusClickListener {
        void onStatusClick(int position, String status);
    }

    public StatusAdapter(ArrayList<String> statusList, int selectedItem, OnStatusClickListener listener) {
        this.statusList = statusList;
        this.selectedItem = selectedItem;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_item, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String status = statusList.get(position);
        holder.statusButton.setText(status);

        // ✅ Change appearance based on selection
        if (position == selectedItem) {
            holder.statusButton.setBackgroundResource(R.drawable.rounded_button); // Active style
            holder.statusButton.setTextColor(Color.WHITE);
        } else {
            holder.statusButton.setBackgroundResource(R.drawable.rounded_button_inactive); // Inactive style
            holder.statusButton.setTextColor(Color.BLACK);
        }

        // ✅ Handle click
        holder.statusButton.setOnClickListener(v -> {
            int previousSelected = selectedItem;
            selectedItem = position;

            // Refresh only changed items for performance
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedItem);

            if (listener != null) {
                listener.onStatusClick(position, status);
            }
        });
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        TextView statusButton;

        public StatusViewHolder(View itemView) {
            super(itemView);
            statusButton = itemView.findViewById(R.id.statusButton);
        }
    }
}


