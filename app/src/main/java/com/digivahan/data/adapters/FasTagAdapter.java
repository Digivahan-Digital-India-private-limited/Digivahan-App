package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.FasTagCardModel;
import com.digivahan.utils.CommonLogic;

import java.util.List;

public class FasTagAdapter extends RecyclerView.Adapter<FasTagAdapter.ViewHolder> {

    private List<FasTagCardModel> list;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FasTagCardModel model);
        void onDeleteClick(FasTagCardModel model, int position);
    }

    public FasTagAdapter(Context context, List<FasTagCardModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fastag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FasTagCardModel model = list.get(position);

        holder.tvBillerName.setText(model.getBillerName());
        holder.tvVehicle.setText(model.getVehicleNumber());

        CommonLogic.loadSvg(context, holder.bankIcon, model.getIconUrl());

        // Click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
            }
        });

        // Delete click
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(model, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBillerName, tvVehicle;
        ImageView btnDelete, bankIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBillerName = itemView.findViewById(R.id.tvBillerName);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            bankIcon = itemView.findViewById(R.id.bankIcon);
        }
    }
}
