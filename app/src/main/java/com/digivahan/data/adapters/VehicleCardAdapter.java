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
import com.digivahan.databinding.ItemVehicleCardBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class VehicleCardAdapter extends RecyclerView.Adapter<VehicleCardAdapter.CardViewHolder> {

    private ArrayList<GarageItemModel> vehicleList; // Replace String with your Vehicle model
    private Context context;

    public VehicleCardAdapter(Context context, ArrayList<GarageItemModel> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleCardBinding binding = ItemVehicleCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CardViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (!vehicleList.isEmpty()) {
            int actualPosition = position % vehicleList.size(); // Loop infinitely
            GarageItemModel vehicle = vehicleList.get(actualPosition);


            // Example binding
            holder.binding.tvCarNo.setText(vehicle.getVehicle_number());
            holder.binding.carOwner.setText(vehicle.getOwner_name());
            holder.binding.carModel.setText(vehicle.getVehicle_name());
            holder.binding.registrationData.setText("Registered on " + TimeUtils.convertDateFormat(vehicle.getRegistration_date(), "dd-MM-yyyy"));

            Glide.with(context)
                    .load(CommonMethods.getVehiclePlaceholder(vehicle.getVehicle_class(), vehicle.getVehicle_name(), vehicle.getMakers_model()))
                    .override(800, 800)   // 🔥 LIMIT SIZE
                    .centerInside()
                    .into(holder.binding.ivCar);

            if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(context,"dd-MM-yyyy hh:mm a"), vehicle.getPollution_expiry())
            || TimeUtils.isDateExpired(CommonMethods.getCurrentDate(context,"dd-MM-yyyy hh:mm a"), vehicle.getInsurance_expiry())){
                holder.binding.pucInsuranceExpiryLayout.setVisibility(View.VISIBLE);

                if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(context,"dd-MM-yyyy hh:mm a"), vehicle.getInsurance_expiry())){
                    holder.binding.tvInsuranceDate.setText("On " + TimeUtils.convertDateFormat(vehicle.getInsurance_expiry(), "dd-MM-yyyy"));
                    holder.binding.insuranceLayout.setVisibility(View.VISIBLE);
                }else {
                    holder.binding.insuranceLayout.setVisibility(View.GONE);
                }
                if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(context,"dd-MM-yyyy hh:mm a"), vehicle.getPollution_expiry())){
                    holder.binding.tvPucDate.setText("On " + TimeUtils.convertDateFormat(vehicle.getPollution_expiry(), "dd-MM-yyyy"));
                    holder.binding.pucLayout.setVisibility(View.VISIBLE);
                }else {
                    holder.binding.pucLayout.setVisibility(View.GONE);
                }

                holder.binding.carDetailsLayout.setVisibility(View.GONE);
                holder.binding.mainCardLayout.setBackgroundResource(R.color.card_color);
                holder.binding.cardLayout.setBackgroundResource(R.drawable.puc_expired_background);
            }else {
                holder.binding.mainCardLayout.setBackgroundResource(R.drawable.bg_card4);
                holder.binding.cardLayout.setBackground(null);
                holder.binding.pucInsuranceExpiryLayout.setVisibility(View.GONE);
                holder.binding.carDetailsLayout.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent vehicleInfoPage = new Intent(context, VehicleInformation.class);
                vehicleInfoPage.putExtra("vehicleData", vehicle);
                context.startActivity(vehicleInfoPage);
            });
        }

    }

    @Override
    public int getItemCount() {
        // Large number for infinite effect
        return Integer.MAX_VALUE;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ItemVehicleCardBinding binding;

        public CardViewHolder(ItemVehicleCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


