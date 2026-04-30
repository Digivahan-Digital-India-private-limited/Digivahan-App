package com.digivahan.data.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.NotificationItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.qr.VirtualQR;
import com.bumptech.glide.Glide;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class VirtualQRItemAdapter extends RecyclerView.Adapter<VirtualQRItemAdapter.VQRIViewHolder> {

    Context context;
    ArrayList<GarageItemModel> list;
    PreferencesManager manager;
    private BaseActivity baseActivity;

    public VirtualQRItemAdapter(Context context, ArrayList<GarageItemModel> list) {
        this.context = context;
        this.list = list;
        manager = new PreferencesManager(context);

        if (context instanceof BaseActivity) {
            this.baseActivity = (BaseActivity) context;
        }

    }

    @NonNull
    @Override
    public VirtualQRItemAdapter.VQRIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationItemDesignBinding binding = NotificationItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VirtualQRItemAdapter.VQRIViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VirtualQRItemAdapter.VQRIViewHolder holder, int position) {

        GarageItemModel model = list.get(position);

        holder.binding.imgProfile.setVisibility(View.GONE);
        holder.binding.carImage.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(CommonMethods.getVehiclePlaceholder(model.getVehicle_class(), model.getVehicle_name(), model.getMakers_model(), model.getCategory())) // Your image URL or file
                .override(800, 800)   // 🔥 LIMIT SIZE
                .centerInside()
                .into(holder.binding.carImage);

        holder.binding.tvName.setText(model.getOwner_name());
        holder.binding.tvTime.setText(model.getVehicle_number());
        holder.binding.tvSubtitle.setText(model.getMakers_model());
        holder.binding.btnChatNow.setText("Preview");
        holder.binding.btnChatNow.setVisibility(View.VISIBLE);

        holder.binding.btnChatNow.setOnClickListener(v -> {
            Intent virtualQRPage = new Intent(context, VirtualQR.class);
            virtualQRPage.putExtra("virtualQRDetails", model);
            baseActivity.disableHideContentSecureForNextNavigation();
            context.startActivity(virtualQRPage);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VQRIViewHolder extends RecyclerView.ViewHolder {
        NotificationItemDesignBinding binding;
        public VQRIViewHolder(NotificationItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
