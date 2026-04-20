package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.NewServicesItemModel;
import com.digivahan.databinding.ItemVehicleCardBinding;
import com.digivahan.databinding.NewServiceCardDesignBinding;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.ui.Activities.qr.ScanQRCode;
import com.digivahan.utils.CommonMethods;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class NewServicesAdapter extends RecyclerView.Adapter<NewServicesAdapter.CardViewHolder> {

    private ArrayList<NewServicesItemModel> servicesItemList; // Replace String with your Vehicle model
    private Activity context;
    PreferencesManager manager;

    private final int[] lightColors = new int[]{
            Color.parseColor("#F4FBF4"),
            Color.parseColor("#EDF9ED"),
            Color.parseColor("#E6F7E6"),
            Color.parseColor("#DFF5DF")/*,
            Color.parseColor("#D8F3D8"),
            Color.parseColor("#D1F1D1"),
            Color.parseColor("#CAF0CA"),
            Color.parseColor("#C3EEC3")*/
    };


    public NewServicesAdapter(Activity context, ArrayList<NewServicesItemModel> servicesItemList) {
        this.context = context;
        this.servicesItemList = servicesItemList;
        manager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NewServiceCardDesignBinding binding = NewServiceCardDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CardViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (!servicesItemList.isEmpty()) {
            int actualPosition = position % servicesItemList.size(); // Loop infinitely
            NewServicesItemModel serviceItem = servicesItemList.get(actualPosition);

            int randomColor = lightColors[new Random().nextInt(lightColors.length)];
            holder.binding.backgroundLayout.setBackgroundColor(randomColor);

            // Example binding
            holder.binding.tvTitle.setText(serviceItem.getTitle());
            holder.binding.tvDescription.setText(serviceItem.getDescription());
            holder.binding.serviceBtn.setText(serviceItem.getButtonText());

            Glide.with(context)
                    .load(serviceItem.getResource())
                    .override(800, 800)   // 🔥 LIMIT SIZE
                    .centerInside()
                    .into(holder.binding.serviceIcon);


            holder.binding.serviceBtn.setOnClickListener(v -> {
                if (serviceItem.getClassName().equalsIgnoreCase("vehicleInfo") || serviceItem.getClassName().equalsIgnoreCase("challanInfo")){

                    String serviceType = "checkVehicle";

                    if (serviceItem.getClassName().equalsIgnoreCase("challanInfo")){
                        serviceType = "challanInfo";
                    }

                    CommonMethods.showAddVehiclePopupDialog(
                            context,
                            serviceType,
                            manager.getUserId(),
                            "",
                            "",
                            new CommonMethods.AddVehicleCallback() {
                                @Override
                                public void onSuccess(GarageItemModel vehicleInfo) {
                                    ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                                    Intent vehicleInfoPage = new Intent(context, VehicleInformation.class);
                                    vehicleInfoPage.putExtra("vehicleData", vehicleInfo);
                                    vehicleInfoPage.putExtra("vehicleDataType", "check");
                                    context.startActivity(vehicleInfoPage);
                                }

                                @Override
                                public void onFailure(String message) {
                                    Toast.makeText(context, "Vehicle not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
                else {
                    openService(serviceItem.getClassName());
                }
            });
        }

    }

    private void openService(String className) {

        Class serviceClass = null;

        if (className.equalsIgnoreCase("VehicleInformation")) {
            serviceClass = VehicleInformation.class;
        }
        else if (className.equalsIgnoreCase("garage")) {
            serviceClass = MyGarageActivity.class;
        }
        else if (className.contains("scan")) {
            serviceClass = ScanQRCode.class;
        }

        if (serviceClass == null){
            Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show();
            return;
        }

        ((BaseActivity) context).disableHideContentSecureForNextNavigation();
        Intent redirectToServicePage = new Intent(context, serviceClass);
        if (className.equalsIgnoreCase("scanConnect")){
            redirectToServicePage.putExtra("scanType", "connect");
        }else if (className.equalsIgnoreCase("scanAssign")){
            redirectToServicePage.putExtra("scanType", "assign");
        }
        context.startActivity(redirectToServicePage);
    }

    @Override
    public int getItemCount() {
        // Large number for infinite effect
        return Integer.MAX_VALUE;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        NewServiceCardDesignBinding binding;

        public CardViewHolder(NewServiceCardDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


