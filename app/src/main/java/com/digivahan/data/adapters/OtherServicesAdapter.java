package com.digivahan.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.data.model.OtherServicesModel;
import com.digivahan.databinding.NotificationItemDesignBinding;
import com.digivahan.databinding.OtherServicesItemDesignBinding;

import java.util.ArrayList;

public class OtherServicesAdapter extends RecyclerView.Adapter<OtherServicesAdapter.NIViewHolder> {

    Context context;
    ArrayList<OtherServicesModel> list;

    public OtherServicesAdapter(Context context, ArrayList<OtherServicesModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public OtherServicesAdapter.NIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        OtherServicesItemDesignBinding binding = OtherServicesItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new NIViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherServicesAdapter.NIViewHolder holder, int position) {

        holder.binding.ivVehicleIcon.setImageResource(list.get(position).getDrawableFile());
        holder.binding.tvVehicleInfo.setText(list.get(position).getServiceTitle());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NIViewHolder extends RecyclerView.ViewHolder {
        OtherServicesItemDesignBinding binding;
        public NIViewHolder(OtherServicesItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
