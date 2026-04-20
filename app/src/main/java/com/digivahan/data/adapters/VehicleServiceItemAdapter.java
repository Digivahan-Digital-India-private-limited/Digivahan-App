package com.digivahan.data.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.data.model.TipsItemModel;
import com.digivahan.data.model.VehicleServiceItemModel;
import com.digivahan.databinding.TipsItemDesignBinding;
import com.digivahan.databinding.VehicleServicesItemDesignBinding;
import com.digivahan.ui.Activities.BBPSServices.FasTag.SelectVehicleBBPS;
import com.digivahan.ui.Activities.BaseActivity;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.garage.MyGarageActivity;
import com.digivahan.ui.Activities.garage.VehicleInformation;
import com.digivahan.ui.Activities.qr.ScanQRCode;
import com.digivahan.ui.Activities.tips.TipsDetailsActivity;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;

public class VehicleServiceItemAdapter extends RecyclerView.Adapter<VehicleServiceItemAdapter.CLViewHolder> {
    String TAG = "VehicleServiceItemAdapterData";
    Context context;
    ArrayList<VehicleServiceItemModel> list;
    PreferencesManager manager;

    public VehicleServiceItemAdapter(Context context, ArrayList<VehicleServiceItemModel> list) {
        this.context = context;
        this.list = list;
        manager = new PreferencesManager(context);
    }

    @NonNull
    @Override
    public VehicleServiceItemAdapter.CLViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VehicleServicesItemDesignBinding binding = VehicleServicesItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CLViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleServiceItemAdapter.CLViewHolder holder, int position) {
        VehicleServiceItemModel vehicleServiceItemModel = list.get(position);

        holder.binding.icon.setImageResource(vehicleServiceItemModel.getIcon());

        holder.binding.title.setText(vehicleServiceItemModel.getTitle());

        holder.binding.getRoot().setOnClickListener(v -> {
            if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("scan_qr")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent userRequestPage = new Intent(context, ScanQRCode.class);
                userRequestPage.putExtra("scanType", "connect");
                context.startActivity(userRequestPage);
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("check_vehicle")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                CommonMethods.showAddVehiclePopupDialog(
                        (Activity) context,
                        "check",
                        manager.getUserId(),
                        "",
                        "",
                        new CommonMethods.AddVehicleCallback() {
                            @SuppressLint("UseRequireInsteadOfGet")
                            @Override
                            public void onSuccess(GarageItemModel vehicleInfo) {
                                ((BaseActivity)context).disableHideContentSecureForNextNavigation();
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
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("check_challan")){
                CommonMethods.showAddVehiclePopupDialog(
                        (Activity) context,
                        "challanInfo",
                        manager.getUserId(),
                        "",
                        "",
                        new CommonMethods.AddVehicleCallback() {
                            @Override
                            public void onSuccess(GarageItemModel vehicleInfo) {

                            }

                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(context, "Vehicle not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("FASTag")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent userRequestPage = new Intent(context, SelectVehicleBBPS.class);
                userRequestPage.putExtra("categoryKey", "C10");
                context.startActivity(userRequestPage);
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("challan_pay")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent userRequestPage = new Intent(context, SelectVehicleBBPS.class);
                userRequestPage.putExtra("categoryKey", "C31");
                context.startActivity(userRequestPage);
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("activate_qr")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent userRequestPage = new Intent(context, ScanQRCode.class);
                userRequestPage.putExtra("scanType", "assign");
                context.startActivity(userRequestPage);
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("my_garage")){
                ((BaseActivity) context).disableHideContentSecureForNextNavigation();
                Intent userRequestPage = new Intent(context, MyGarageActivity.class);
                context.startActivity(userRequestPage);
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("download_qr_code")){
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openFragment(
                            R.id.nav_virtualQR,   // target fragment id
                            false,                // showBottomNav
                            true                 // showNotificationIcon
                    );
                }
            } else if (vehicleServiceItemModel.getServiceType().equalsIgnoreCase("order_qr_code")){
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openFragment(
                            R.id.nav_order,   // target fragment id
                            false,                // showBottomNav
                            true                 // showNotificationIcon
                    );
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CLViewHolder extends RecyclerView.ViewHolder {
        VehicleServicesItemDesignBinding binding;
        public CLViewHolder(VehicleServicesItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
