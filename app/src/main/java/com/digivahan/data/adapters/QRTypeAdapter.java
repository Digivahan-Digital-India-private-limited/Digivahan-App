package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.data.model.QRTypeModel;
import com.digivahan.databinding.CheckQrStatusDesignBinding;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.orderDetails.TrackOrderPage;

import java.util.ArrayList;

public class QRTypeAdapter extends RecyclerView.Adapter<QRTypeAdapter.CLViewHolder> {

    Context context;
    ArrayList<QRTypeModel> list;

    public QRTypeAdapter(Context context, ArrayList<QRTypeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public QRTypeAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckQrStatusDesignBinding binding = CheckQrStatusDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QRTypeAdapter.CLViewHolder holder, int position) {
        holder.binding.checkStatusButton.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent documentPage = new Intent(context, TrackOrderPage.class);
            context.startActivity(documentPage);
        });

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        CheckQrStatusDesignBinding binding;
        public CLViewHolder(CheckQrStatusDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
