package com.digivahan.data.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.AppDatabase;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.ItemGarageCardDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.utils.AppExecutors;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

public class GarageItemAdapter extends RecyclerView.Adapter<GarageItemAdapter.CLViewHolder> {

    String TAG = "GarageItemAdapterData";

    Activity context;
    ArrayList<GarageItemModel> list;
    boolean isEditable;
    PreferencesManager preferencesManager;
    AshDialog loadingDialog;

    OnVehicleDeleteListener deleteListener;


    public GarageItemAdapter(Activity context, ArrayList<GarageItemModel> list, boolean isEditable, AshDialog loadingDialog,  OnVehicleDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.isEditable = isEditable;
        this.loadingDialog = loadingDialog;
        this.deleteListener = deleteListener;
        preferencesManager = new PreferencesManager(context);
    }

    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public GarageItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGarageCardDesignBinding binding = ItemGarageCardDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GarageItemAdapter.CLViewHolder holder, int position) {
        GarageItemModel model = list.get(position);

        if (isEditable){
            holder.binding.deleteBtn.setVisibility(View.VISIBLE);
        }else {holder.binding.deleteBtn.setVisibility(View.GONE);}

        holder.binding.tvCarName.setText(model.getMakers_model());
        holder.binding.tvCarDetails.setText(model.getMakers_name());
        holder.binding.tvCarNumber.setText(model.getVehicle_number());

//        ImageHelperMethods.loadImage(TAG, context, "", holder.binding.ivCar, CommonMethods.getVehiclePlaceholder(model.getVehicle_class()));

        Glide.with(context)
                .load(CommonMethods.getVehiclePlaceholder(model.getVehicle_class(), model.getVehicle_name(), model.getMakers_model()))
                .override(800, 800)   // 🔥 LIMIT SIZE
                .centerInside()
                .into(holder.binding.ivCar);


        CommonLogic.showTestLog(TAG, "model Data: " + model.getVehicle_name() +"Vehicle model: " + model.getMakers_model() + "Car Number: " + model.getVehicle_number());

        holder.binding.garageItemLayout.setOnClickListener(v -> {
            if (!isEditable) {
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent vehicleInfoPage = new Intent(context, VehicleInformation.class);
                vehicleInfoPage.putExtra("vehicleData", model);
                context.startActivity(vehicleInfoPage);
            }
        });

        holder.binding.deleteBtn.setOnClickListener(v -> {
            deleteVehicle(model.getVehicle_id(), position);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        ItemGarageCardDesignBinding binding;
        public CLViewHolder(ItemGarageCardDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void deleteVehicleChallanData(String vehicleNumber) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(context)
                    .vehicleChallanDao()
                    .deleteByVehicleNumber(vehicleNumber);

            CommonLogic.showTestLog(TAG,
                    "🗑️ Challan data deleted for vehicle: " + vehicleNumber);
        });
    }


    public interface OnVehicleDeleteListener {
        void onVehicleDeleted(String vehicleNumber, int position, int listSize);
        void onVehicleDeleteFailed(String errorMessage);
    }


    private void deleteVehicle(String vehicleNumber, int itemPosition) {

        new AlertDialog.Builder(context)
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to remove this vehicle?")
                .setCancelable(false)
                .setPositiveButton("Yes, delete", (dialog, which) -> {

                    loadingDialog.show();

                    JsonObject jsonObjectDeleteVehicle = new JsonObject();
                    jsonObjectDeleteVehicle.addProperty("user_id", preferencesManager.getUserId());
                    jsonObjectDeleteVehicle.addProperty("vehicle_number", vehicleNumber);

                    ApiCall.callApi(
                            TAG,
                            context,
                            APIData.DELETE_VEHICLE_ITEM,
                            jsonObjectDeleteVehicle,
                            "post",
                            new ApiCall.ApiResponseCallback() {

                                @Override
                                public void onSuccess(JSONObject responseBody, boolean status, String message) {

                                    loadingDialog.dismiss();

                                    if (status) {

                                        deleteVehicleChallanData(vehicleNumber);

                                        list.remove(itemPosition);
                                        notifyItemRemoved(itemPosition);
                                        notifyItemRangeChanged(itemPosition, list.size());

                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                                        // 🔥 CALLBACK SUCCESS
                                        if (deleteListener != null) {
                                            deleteListener.onVehicleDeleted(vehicleNumber, itemPosition, list.size());
                                        }

                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                                        if (deleteListener != null) {
                                            deleteListener.onVehicleDeleteFailed(message);
                                        }
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {

                                    loadingDialog.dismiss();

                                    if (deleteListener != null) {
                                        deleteListener.onVehicleDeleteFailed(errorMessage);
                                    }
                                }
                            }
                    );
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
