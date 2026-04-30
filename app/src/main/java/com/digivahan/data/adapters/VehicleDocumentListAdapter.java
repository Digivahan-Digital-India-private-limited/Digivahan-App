package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digivahan.R;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.VehicleDocumentListItemDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.garage.ViewDocumentPage;
import com.digivahan.utils.CommonLogic;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

public class VehicleDocumentListAdapter extends RecyclerView.Adapter<VehicleDocumentListAdapter.CLViewHolder> {
    String TAG = "VehicleDocumentListAdapterData", vehicleId = "";
    Activity context;
    ArrayList<GarageItemModel.vehicleDocuments> list;

    AshDialog loadingDialog;
    PreferencesManager manager;

    OnDeleteClickListener deleteClickListener;

    boolean isEditable = false;

    public interface OnDeleteClickListener {
        void onDeleteClick(int listSize);
    }


    public VehicleDocumentListAdapter(Activity context, String vehicleId, boolean isEditable, ArrayList<GarageItemModel.vehicleDocuments> list) {
        this.context = context;
        this.vehicleId = vehicleId;
        this.isEditable = isEditable;
        this.list = list;
        loadingDialog = new AshDialog(context, "Please wait", "");
        manager = new PreferencesManager(context);
    }

    public VehicleDocumentListAdapter(Activity context, String vehicleId, boolean isEditable,
                                      ArrayList<GarageItemModel.vehicleDocuments> list,
                                      OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.vehicleId = vehicleId;
        this.isEditable = isEditable;
        this.list = list;
        this.deleteClickListener = deleteClickListener;

        loadingDialog = new AshDialog(context, "Please wait", "");
        manager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public VehicleDocumentListAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VehicleDocumentListItemDesignBinding binding = VehicleDocumentListItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleDocumentListAdapter.CLViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GarageItemModel.vehicleDocuments vehicleDocuments = list.get(position);

        if (isEditable){
            holder.binding.deleteDocument.setVisibility(ViewGroup.VISIBLE);
        } else {
            holder.binding.deleteDocument.setVisibility(ViewGroup.GONE);
        }

        holder.binding.documentName.setText(vehicleDocuments.getDoc_name());
        holder.binding.documentNumber.setText(vehicleDocuments.getDoc_number());

        holder.binding.viewDocument.setOnClickListener(v -> {
            ((BaseActivity) context).disableHideContentSecureForNextNavigation();
            Intent viewDocument = new Intent(context, ViewDocumentPage.class);
            viewDocument.putExtra("docUrl", vehicleDocuments.getDoc_url());
            context.startActivity(viewDocument);
        });

        if (vehicleDocuments.getDoc_type().equalsIgnoreCase("insurance")){
            holder.binding.docIcon.setImageResource(R.drawable.insurance_icon);
        }
        else if (vehicleDocuments.getDoc_type().equalsIgnoreCase("Aadhar")){
            holder.binding.docIcon.setImageResource(R.drawable.aadhaar_icon);
        }
        else if (vehicleDocuments.getDoc_type().equalsIgnoreCase("Pollution")){
            holder.binding.docIcon.setImageResource(R.drawable.pollution_icon);
        }
        else if (vehicleDocuments.getDoc_type().equalsIgnoreCase("RC")){
            holder.binding.docIcon.setImageResource(R.drawable.registration_certificate_icon1);
        }
        else if (vehicleDocuments.getDoc_type().equalsIgnoreCase("Pancard")){
            holder.binding.docIcon.setImageResource(R.drawable.aadhaar_icon);
        }
        else if (vehicleDocuments.getDoc_type().equalsIgnoreCase("Driving Licence")){
            holder.binding.docIcon.setImageResource(R.drawable.aadhaar_icon);
        }

        holder.binding.deleteDocument.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Document?")
                    .setMessage("Are you sure you want to delete it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes, Delete", (dialog, which) -> {
                        JsonObject jsonObjectGarageVehicle = new JsonObject();
                        jsonObjectGarageVehicle.addProperty("user_id", manager.getUserId());
                        jsonObjectGarageVehicle.addProperty("vehicle_id", vehicleId);
                        jsonObjectGarageVehicle.addProperty("doc_type", vehicleDocuments.getDoc_type());

                        CommonLogic.showTestLog(TAG, "Params: "+ jsonObjectGarageVehicle);

                        loadingDialog.show();

                        ApiCall.callApi(TAG,
                                context,
                                APIData.DELETE_VEHICLE_FILE,
                                jsonObjectGarageVehicle, "post", new ApiCall.ApiResponseCallback() {
                                    @Override
                                    public void onSuccess(JSONObject responseBody, boolean status, String message) {
                                        list.remove(position);
                                        notifyDataSetChanged();
                                        loadingDialog.dismiss();
                                        if (deleteClickListener != null) {
                                            deleteClickListener.onDeleteClick(list.size());
                                        }
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(context, "Unable to delete document, try after some time.", Toast.LENGTH_SHORT).show();
                                        CommonLogic.showTestLog(TAG, errorMessage);
                                        loadingDialog.dismiss();
                                    }
                                });
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        VehicleDocumentListItemDesignBinding binding;

        public CLViewHolder(VehicleDocumentListItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
