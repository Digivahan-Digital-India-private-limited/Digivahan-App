package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.NotificationItemDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.orderDetails.OrderQRPage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class MyOrderItemAdapter extends RecyclerView.Adapter<MyOrderItemAdapter.NIViewHolder> {

    Context context;
    ArrayList<GarageItemModel> list;

    public MyOrderItemAdapter(Context context, ArrayList<GarageItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyOrderItemAdapter.NIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        NotificationItemDesignBinding binding = NotificationItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new NIViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyOrderItemAdapter.NIViewHolder holder, int position) {

        GarageItemModel model = list.get(position);

        holder.binding.imgProfile.setVisibility(View.GONE);
        holder.binding.carImage.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(CommonMethods.getVehiclePlaceholder(model.getVehicle_class(), model.getVehicle_name(), model.getMakers_model())) // Your image URL or file
                .override(800, 800)   // 🔥 LIMIT SIZE
                .centerInside()
                .into(holder.binding.carImage);

        holder.binding.tvName.setText(model.getOwner_name());
        holder.binding.tvTime.setText(model.getVehicle_number());
        holder.binding.tvSubtitle.setText(model.getMakers_model());
        holder.binding.btnChatNow.setText("Order Now");

        holder.binding.btnChatNow.setVisibility(View.VISIBLE);

        holder.binding.btnChatNow.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent orderDetailsPage = new Intent(context, OrderQRPage.class);
            orderDetailsPage.putExtra("vehicleDetails", model);
            orderDetailsPage.putExtra("orderType", "vehicle");
            orderDetailsPage.putExtra("qrFor", "vehicle: "+model.getVehicle_name());
            context.startActivity(orderDetailsPage);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NIViewHolder extends RecyclerView.ViewHolder {
        NotificationItemDesignBinding binding;
        public NIViewHolder(NotificationItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
