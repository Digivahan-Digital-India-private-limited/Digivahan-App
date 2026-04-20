package com.digivahan.ui.Activities.garage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.GarageItemAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.ActivityMyGarageBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.digivahan.utils.NetworkUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class  MyGarageActivity extends BaseActivity {

    String TAG = "MyGarageActivityData";
    ActivityMyGarageBinding binding;

    ArrayList<GarageItemModel> garageItemList = new ArrayList<>();
    GarageItemAdapter orderListItemAdapter;
    PreferencesManager preferencesManager;

    boolean isEditable = false;
    AshDialog loadingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<GarageItemModel> cachedList = CommonMethods.loadGarageCache(MyGarageActivity.this);

        if (cachedList != null && !cachedList.isEmpty()){
            updateGarageUI(cachedList);
        }

        getGarageVehicleList();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGarageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        preferencesManager = new PreferencesManager(MyGarageActivity.this);

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setImageResource(R.drawable.edit_icon2);
        binding.toolbarLayout.tvTitle.setText("My Garage");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        getOnBackPressedDispatcher().addCallback(MyGarageActivity.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditable){
                    isEditable = false;
                    binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
                    binding.btnAddVehicle.setVisibility(View.VISIBLE);
                    orderListItemAdapter.setEditable(isEditable);
                    orderListItemAdapter.notifyDataSetChanged();

                    setEmptyLayout(garageItemList.isEmpty());
                }
                else {
                    disableHideContentSecureForNextNavigation();
                    finish();
                }
            }
        });

        loadingDialog = new AshDialog(MyGarageActivity.this, "Please wait", "");

        orderListItemAdapter = new GarageItemAdapter(
                this,
                garageItemList,
                isEditable,
                loadingDialog,
                new GarageItemAdapter.OnVehicleDeleteListener() {

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onVehicleDeleted(String vehicleNumber, int position, int listSize) {
                        isEditable = false;
                        orderListItemAdapter.setEditable(isEditable);
                        orderListItemAdapter.notifyDataSetChanged();
                        setEmptyLayout(listSize <= 0);

                        String json = new Gson().toJson(garageItemList);
                        new PreferencesManager(MyGarageActivity.this).setString(PreferencesManager.KEY_GARAGE_CACHE, json);
                    }

                    @Override
                    public void onVehicleDeleteFailed(String errorMessage) {
                        Toast.makeText(
                                MyGarageActivity.this,
                                errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );


        binding.rvGarageList.setAdapter(orderListItemAdapter);

        binding.btnAddVehicle.setOnClickListener(v -> {
            showAddVehicleBottomSheet();
        });

        binding.toolbarLayout.ivBell.setOnClickListener(v -> {
            isEditable = true;
            binding.toolbarLayout.ivBell.setVisibility(View.GONE);
            binding.btnAddVehicle.setVisibility(View.INVISIBLE);
            orderListItemAdapter.setEditable(isEditable);
            orderListItemAdapter.notifyDataSetChanged();
        });

    }

    private void getGarageVehicleList() {

        CommonLogic.showTestLog(TAG, "user id" + preferencesManager.getUserId());

        loadingDialog.show();
        CommonMethods.getGarageVehicleList(TAG,
                MyGarageActivity.this,
                preferencesManager.getUserId(),
                new CommonMethods.GarageListCallback() {

                    @Override
                    public void onSuccess(ArrayList<GarageItemModel> garageList) {

                        loadingDialog.dismiss();

                        updateGarageUI(garageList);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingDialog.dismiss();
                        setEmptyLayout(true);
                        CommonLogic.showTestLog(TAG, "getGarageVehicleList: " + errorMessage);

                        if (errorMessage.equalsIgnoreCase("No internet connection")){
                            showNoInternetDialog(MyGarageActivity.this);
                        }else {
                        Toast.makeText(MyGarageActivity.this, "Vehicle not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void showNoInternetDialog(Activity activity) {
        // Create a dialog instance
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.crop_image_dialog_design);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setCancelable(false);

        // Initialize dialog elements
        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        TextView btnNo = dialog.findViewById(R.id.btnNo);
        TextView btnYes = dialog.findViewById(R.id.btnYes);

//        dialogImage.setImageResource(R.drawable.internet_connection_icon);

        btnNo.setText("Cancel");
        btnYes.setText("Retry");
        dialogTitle.setText("No Internet Connection");
        dialogTitle.setVisibility(View.VISIBLE);
        dialogMessage.setText("Please check your internet connection and try again.");

        // true condition if user not have dl
        // Handle button clicks
        btnNo.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
            dialog.dismiss();
        });
        btnYes.setOnClickListener(v -> {
            // Perform action on OK
            if (NetworkUtils.isInternetAvailable(MyGarageActivity.this)) {

                dialog.dismiss();

                // retry API
                getGarageVehicleList();

            } else {
                Toast.makeText(MyGarageActivity.this, "Still no internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void updateGarageUI(ArrayList<GarageItemModel> garageList) {
        garageItemList.clear();
        garageItemList.addAll(garageList);

        CommonLogic.showTestLog(TAG, "List size: garageList " + garageList.size() + " garageItemList " + garageItemList.size());

        CommonLogic.showTestLog(TAG, "model Data: " + garageItemList.get(0).getVehicle_name() +"Vehicle model: " + garageItemList.get(0).getMakers_model() + "Car Number: " + garageItemList.get(0).getVehicle_number());
        CommonLogic.showTestLog(TAG, "model Data: " + garageList.get(0).getVehicle_name() +"Vehicle model: " + garageList.get(0).getMakers_model() + "Car Number: " + garageList.get(0).getVehicle_number());

        setEmptyLayout(garageList.isEmpty());

        orderListItemAdapter.notifyDataSetChanged();
    }

    private void setEmptyLayout(boolean isEmpty){

        if (isEditable){
            binding.toolbarLayout.ivBell.setVisibility(View.GONE);
            binding.btnAddVehicle.setVisibility(View.GONE);
        }else {
            binding.toolbarLayout.ivBell.setVisibility(View.VISIBLE);
            binding.btnAddVehicle.setVisibility(View.VISIBLE);
        }

        if (isEmpty){
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.rvGarageList.setVisibility(View.GONE);
        }else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.rvGarageList.setVisibility(View.VISIBLE);
        }
    }



    private void showAddVehicleBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_vehicle, null);

        // 👇 Add this line before showing dialog
        bottomSheetDialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        EditText etRcNumber = view.findViewById(R.id.etRcNumber);
        Button btnAddToGarage = view.findViewById(R.id.btnAddToGarage);



        btnAddToGarage.setOnClickListener(v -> {
            String rcNumber = etRcNumber.getText().toString().toUpperCase(Locale.ROOT).trim();
            if (!rcNumber.isEmpty()) {
                loadingDialog.show();

                if (garageItemList != null && !garageItemList.isEmpty()) {
                    for (GarageItemModel itemModel : garageItemList) {
                        if (itemModel.getVehicle_id().equalsIgnoreCase(rcNumber)) {
                            disableHideContentSecureForNextNavigation();
                            Intent vehicleInfoPage = new Intent(MyGarageActivity.this, VehicleInformation.class);
                            vehicleInfoPage.putExtra("vehicleData", itemModel);
                            vehicleInfoPage.putExtra("vehicleDataType", "verity");
                            startActivity(vehicleInfoPage);
                            loadingDialog.dismiss();
                            bottomSheetDialog.dismiss();
                            return;
                        }
                    }
                }


                JsonObject jsonObjectFuelPrice = new JsonObject();
                jsonObjectFuelPrice.addProperty("vehicle_number", rcNumber);

                ApiCall.callApi(TAG,
                        MyGarageActivity.this,
                        APIData.CHECK_VEHICLE,
                        jsonObjectFuelPrice, "post",
                        new ApiCall.ApiResponseCallback() {
                            @Override
                            public void onSuccess(JSONObject responseBody, boolean status, String message) {
                                try {
                                    CommonLogic.showTestLog(TAG, "showAddVehicleBottomSheet: " + responseBody.toString());

                                    if (responseBody.has("http_code") && responseBody.getInt("http_code") == 503){
                                        CommonMethods.showMessageDialog(MyGarageActivity.this, "Under Maintenance",
                                                "RTO under maintenance, Vehicle not found in RTO database, please check the vehicle number or try after some time");
                                        loadingDialog.dismiss();
                                        return;
                                    }

                                    if (status) {
                                        //_______________

                                        Toast.makeText(MyGarageActivity.this, message, Toast.LENGTH_SHORT).show();

                                        JSONObject info = responseBody.getJSONObject("data").getJSONObject("result").optJSONObject("custom_vehicle_info");

                                        GarageItemModel model = new GarageItemModel();

                                        // 🔹 IDs

                                        model.setVehicle_id(rcNumber);

                                        // 🔹 Vehicle Info (log each field)
                                        model.setOwner_name(CommonMethods.getSafeString(info, "owner_name"));
                                        CommonLogic.showTestLog(TAG, "👤 owner_name = " + model.getOwner_name());

                                        model.setVehicle_number(CommonMethods.getSafeString(info, "vehicle_number"));
                                        CommonLogic.showTestLog(TAG, "🚘 vehicle_number = " + model.getVehicle_number());

                                        model.setVehicle_name(CommonMethods.getSafeString(info, "vehicle_name"));
                                        CommonLogic.showTestLog(TAG, "🚗 vehicle_name = " + model.getVehicle_name());

                                        model.setFuel_type(CommonMethods.getSafeString(info, "fuel_type"));
                                        CommonLogic.showTestLog(TAG, "⛽ fuel_type = " + model.getFuel_type());

                                        model.setRc_status(CommonMethods.getSafeString(info, "rc_status"));
                                        CommonLogic.showTestLog(TAG, "📄 rc_status = " + model.getRc_status());

                                        model.setRegistration_date(CommonMethods.getSafeString(info, "registration_date"));
                                        CommonLogic.showTestLog(TAG, "📅 registration_date = " + model.getRegistration_date());

                                        model.setOwnership_details(CommonMethods.getSafeString(info, "ownership_details"));
                                        CommonLogic.showTestLog(TAG, "👥 ownership_details = " + model.getOwnership_details());

                                        model.setRegistered_rto(CommonMethods.getSafeString(info, "registered_rto"));
                                        CommonLogic.showTestLog(TAG, "🏢 registered_rto = " + model.getRegistered_rto());

                                        model.setMakers_model(CommonMethods.getSafeString(info, "makers_model"));
                                        CommonLogic.showTestLog(TAG, "🏭 makers_model = " + model.getMakers_model());

                                        model.setMakers_name(CommonMethods.getSafeString(info, "makers_name"));
                                        CommonLogic.showTestLog(TAG, "🏭 makers_name = " + model.getMakers_name());

                                        model.setVehicle_class(CommonMethods.getSafeString(info, "vehicle_class"));
                                        CommonLogic.showTestLog(TAG, "🚙 vehicle_class = " + model.getVehicle_class());

                                        model.setFuel_norms(CommonMethods.getSafeString(info, "fuel_norms"));
                                        CommonLogic.showTestLog(TAG, "🌱 fuel_norms = " + model.getFuel_norms());

                                        model.setEngine(CommonMethods.getSafeString(info, "engine"));
                                        CommonLogic.showTestLog(TAG, "⚙️ engine = " + model.getEngine());

                                        model.setChassis_number(CommonMethods.getSafeString(info, "chassis_number"));
                                        CommonLogic.showTestLog(TAG, "🔢 chassis_number = " + model.getChassis_number());

                                        model.setInsurer_name(CommonMethods.getSafeString(info, "insurer_name"));
                                        CommonLogic.showTestLog(TAG, "🏥 insurer_name = " + model.getInsurer_name());

                                        model.setInsurance_type(CommonMethods.getSafeString(info, "insurance_type"));
                                        CommonLogic.showTestLog(TAG, "📑 insurance_type = " + model.getInsurance_type());

                                        model.setInsurance_expiry(CommonMethods.getSafeString(info, "insurance_expiry"));
                                        CommonLogic.showTestLog(TAG, "⏳ insurance_expiry = " + model.getInsurance_expiry());

                                        model.setInsurance_renewed_date(CommonMethods.getSafeString(info, "insurance_renewed_date"));
                                        CommonLogic.showTestLog(TAG, "🔄 insurance_renewed_date = " + model.getInsurance_renewed_date());

                                        model.setVehicle_age(CommonMethods.getSafeString(info, "vehicle_age"));
                                        CommonLogic.showTestLog(TAG, "🎂 vehicle_age = " + model.getVehicle_age());

                                        model.setFitness_upto(CommonMethods.getSafeString(info, "fitness_upto"));
                                        CommonLogic.showTestLog(TAG, "✅ fitness_upto = " + model.getFitness_upto());

                                        model.setPollution_renew_date(CommonMethods.getSafeString(info, "pollution_renew_date"));
                                        CommonLogic.showTestLog(TAG, "♻️ pollution_renew_date = " + model.getPollution_renew_date());

                                        model.setPollution_expiry(CommonMethods.getSafeString(info, "pollution_expiry"));
                                        CommonLogic.showTestLog(TAG, "🚫 pollution_expiry = " + model.getPollution_expiry());

                                        model.setColor(CommonMethods.getSafeString(info, "color"));
                                        CommonLogic.showTestLog(TAG, "🎨 color = " + model.getColor());

                                        model.setUnloaded_weight(CommonMethods.getSafeString(info, "unloaded_weight"));
                                        CommonLogic.showTestLog(TAG, "⚖️ unloaded_weight = " + model.getUnloaded_weight());

                                        model.setInsurance_policy_number(
                                                CommonMethods.getSafeString(info, "insurance_policy_number"));
                                        CommonLogic.showTestLog(TAG,
                                                "📜 insurance_policy_number = " + model.getInsurance_policy_number());

                                        disableHideContentSecureForNextNavigation();
                                        Intent vehicleInfoPage = new Intent(MyGarageActivity.this, VehicleInformation.class);
                                        vehicleInfoPage.putExtra("vehicleData", model);
                                        vehicleInfoPage.putExtra("vehicleDataType", "check");
                                        startActivity(vehicleInfoPage);
                                    }else {
                                        CommonMethods.showVehicleNotFoundDialog(MyGarageActivity.this);
                                    }
                                    loadingDialog.dismiss();
                                    bottomSheetDialog.dismiss();
                                } catch (JSONException e) {
                                    CommonLogic.showTestLog(TAG, e.getMessage());
                                    CommonMethods.showVehicleNotFoundDialog(MyGarageActivity.this);
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                bottomSheetDialog.dismiss();
                                loadingDialog.dismiss();
                                CommonLogic.showTestLog(TAG, errorMessage);
                                CommonMethods.showVehicleNotFoundDialog(MyGarageActivity.this);
//                                Toast.makeText(MyGarageActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                );

            } else {
                Toast.makeText(this, "Please enter Vehicle Number", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

}