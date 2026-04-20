package com.digivahan.ui.Activities.garage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.MyPagerAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.ActivityVehicleInformationBinding;
import com.digivahan.databinding.ProfileIncompleteDialogDesignBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class VehicleInformation extends BaseActivity {
    String TAG = "VehicleInformationData";
    ActivityVehicleInformationBinding binding;
    GarageItemModel vehicleInfo;

    AshDialog loadingDialog;
    PreferencesManager manager;

    String vehicleId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.GONE);
        binding.toolbarLayout.tvTitle.setText("Vehicle Info");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> {
            back();
        });

        getOnBackPressedDispatcher().addCallback(VehicleInformation.this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        loadingDialog = new AshDialog(VehicleInformation.this, "Please wait", "");

        manager = new PreferencesManager(VehicleInformation.this);

        try {
            if (getIntent().hasExtra("vehicleData")) {
                vehicleInfo = (GarageItemModel) getIntent().getSerializableExtra("vehicleData");
                vehicleId = Objects.requireNonNull(vehicleInfo).getVehicle_id();
            }else if (getIntent().hasExtra("vehicleId")){
                vehicleId = getIntent().getStringExtra("vehicleId");
                getVehicleData();
            }

            if (getIntent().hasExtra("vehicleDataType") && Objects.requireNonNull(getIntent().getStringExtra("vehicleDataType")).equalsIgnoreCase("check")){
                binding.challanAndExploreLayout.setVisibility(View.GONE);
                binding.addItInMyGarage.setVisibility(View.VISIBLE);

                setVehicleInfo("check");

                for ( GarageItemModel model : CommonMethods.loadGarageCache(VehicleInformation.this)){
                    if (model.getVehicle_id().equalsIgnoreCase(vehicleId)){
                        vehicleInfo = model;
                        setVehicleInfo("verity");
                        break;
                    }
                }

            }else {
                binding.challanAndExploreLayout.setVisibility(View.VISIBLE);
                binding.addItInMyGarage.setVisibility(View.GONE);
                if (vehicleInfo != null) {
                    setVehicleInfo("verity");
                }
            }

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        binding.checkChallan.setOnClickListener(view -> {
            disableHideContentSecureForNextNavigation();
            Intent checkChallan = new Intent(VehicleInformation.this, CheckChallan.class);
            checkChallan.putExtra("vehicleNumber", vehicleInfo.getVehicle_number());
            startActivity(checkChallan);
        });

        binding.refreshVehicleData.setOnClickListener(view -> {
            /*Intent mainPageDashboard = new Intent(VehicleInformation.this, MainActivity.class);
            mainPageDashboard.putExtra("changeFragment", "dashboard");
            mainPageDashboard.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(mainPageDashboard);*/

            refreshVehicleData();
        });

        binding.addItInMyGarage.setOnClickListener(view -> {
            CommonLogic.showTestLog(TAG, "addItInMyGarage Details: uid" + manager.getUserId() + " vid: " +
                    vehicleInfo.getVehicle_id() + " oName: " +
                    vehicleInfo.getOwner_name());
            CommonMethods.showAddVehiclePopupDialog(
                    VehicleInformation.this,
                    "verifyVehicle",
                    manager.getUserId(),
                    vehicleInfo.getVehicle_id(),
                    vehicleInfo.getOwner_name(),
                    new CommonMethods.AddVehicleCallback() {
                        @Override
                        public void onSuccess(GarageItemModel updatedVehicleInfo) {
                            binding.challanAndExploreLayout.setVisibility(View.VISIBLE);
                            binding.addItInMyGarage.setVisibility(View.GONE);
                            vehicleInfo = updatedVehicleInfo;
                            setVehicleInfo("verify");
                        }

                        @Override
                        public void onFailure(String message) {

                        }
                    }
            );

        });

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void setVehicleInfo(String informationType) {
        binding.tabLayout.removeAllTabs();
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Details"));
        if (!informationType.equalsIgnoreCase("check")) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Documents"));
        }
//        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Virtual RC"));

        MyPagerAdapter adapter = new MyPagerAdapter(VehicleInformation.this, VehicleInformation.this, vehicleInfo, binding.tabLayout.getTabCount());
        binding.viewPager.setAdapter(adapter);

// Attach TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Details");
                            break;
                        case 1:
                            tab.setText("Documents");
                            break;
                        case 2:
                            tab.setText("My Virtual RC");
                            break;
                    }
                }).attach();

        binding.tvUserName.setText(vehicleInfo.getOwner_name().isEmpty() ? getString(R.string.empty_string) : vehicleInfo.getOwner_name());
        binding.tvVehicleNumber.setText(vehicleInfo.getVehicle_number() + " | " + CommonMethods.getFormattedOwner(vehicleInfo.getOwnership_details()));
        binding.tvVehicleName.setText(vehicleInfo.getVehicle_name());

        if (!informationType.equalsIgnoreCase("check")) {
            binding.tabLayout.setVisibility(View.VISIBLE);
        }else {
            binding.tabLayout.setVisibility(View.GONE);
        }

//        ImageHelperMethods.loadImage(TAG, VehicleInformation.this, "", binding.ivVehicle, CommonMethods.getVehiclePlaceholder(vehicleInfo.getVehicle_class()));


        Glide.with(VehicleInformation.this)
                .load(CommonMethods.getVehiclePlaceholder(vehicleInfo.getVehicle_class(), vehicleInfo.getVehicle_name(), vehicleInfo.getMakers_model()))
                .override(800, 800)   // 🔥 LIMIT SIZE
                .centerInside()
                .into(binding.ivVehicle);
    }

    private void getVehicleData() {
        if (vehicleId != null && !vehicleId.isEmpty()) {
            loadingDialog.show();
            CommonMethods.getGarageVehicleList(TAG,
                    VehicleInformation.this,
                    manager.getUserId(),
                    new CommonMethods.GarageListCallback() {

                        @Override
                        public void onSuccess(ArrayList<GarageItemModel> garageList) {

                            for (GarageItemModel model : garageList){
                                if (vehicleId.equalsIgnoreCase(model.getVehicle_id())) {
                                    vehicleInfo = model;
                                    setVehicleInfo("verify");
                                    break;
                                }
                            }

                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            loadingDialog.dismiss();
                            CommonLogic.showTestLog(TAG, "getVehicleData error: " + errorMessage);
                            Toast.makeText(VehicleInformation.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        }
    }

    private void refreshVehicleData() {
        if (vehicleId != null && !vehicleId.isEmpty()) {
            loadingDialog.show();

            JsonObject refreshVehicleJsonObject = new JsonObject();
            refreshVehicleJsonObject.addProperty("user_id", manager.getUserId());
            refreshVehicleJsonObject.addProperty("vehicle_id", vehicleId);

            CommonLogic.showTestLog(TAG, "refreshVehicleData params: " + refreshVehicleJsonObject);

            ApiCall.callApi(TAG, VehicleInformation.this, APIData.REFRESH_VEHICLE, refreshVehicleJsonObject, "post", new ApiCall.ApiResponseCallback() {
                @Override
                public void onSuccess(JSONObject responseBody, boolean status, String message) {
                    CommonLogic.showTestLog(TAG, "refreshVehicleData response: " + responseBody.toString());
                    try {

                        if (responseBody.has("http_code") && responseBody.getInt("http_code") == 503){
                            CommonMethods.showMessageDialog(VehicleInformation.this, "Under Maintenance", "RTO under maintenance, Please try after some time");
                            loadingDialog.dismiss();
                            return;
                        }

                        Toast.makeText(VehicleInformation.this, message, Toast.LENGTH_SHORT).show();
                        vehicleInfo = CommonMethods.fetchVehicleData(vehicleInfo , responseBody.getJSONObject("data"));
                        setVehicleInfo("verify");
                    } catch (JSONException e) {
                        Toast.makeText(VehicleInformation.this, "Data is already Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }

                @Override
                public void onError(String errorMessage) {
                    CommonLogic.showTestLog(TAG, "refreshVehicleData onError: " + errorMessage.toString());
                    loadingDialog.dismiss();
                    Toast.makeText(VehicleInformation.this, "Data is already Updated", Toast.LENGTH_SHORT).show();
                }

               /*@Override
                public void onResponseCode(int responseCode) {
                    if (responseCode == 500){
                        Toast.makeText(VehicleInformation.this, "RTO under maintenance", Toast.LENGTH_SHORT).show();
                    }
                }*/
            });

        }
    }


    /*private void showAddVehiclePopupDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_vehicle, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // 🔹 Transparent background (optional but recommended)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }

        EditText etRcNumber = view.findViewById(R.id.etRcNumber);
        Button btnAddToGarage = view.findViewById(R.id.btnAddToGarage);

        btnAddToGarage.setOnClickListener(v -> {
            String rcNumber = etRcNumber.getText().toString()
                    .toUpperCase(Locale.ROOT)
                    .trim();

            if (rcNumber.isEmpty()) {
                Toast.makeText(this, "Please enter Vehicle Number", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            JsonObject jsonObjectFuelPrice = new JsonObject();
            jsonObjectFuelPrice.addProperty("user_id", preferencesManager.getUserId());
            jsonObjectFuelPrice.addProperty("vehicle_number", rcNumber);

            ApiCall.callApi(
                    TAG,
                    VehicleInformation.this,
                    APIData.ADD_VEHICLE,
                    jsonObjectFuelPrice,
                    "post",
                    new ApiCall.ApiResponseCallback() {

                        @Override
                        public void onSuccess(JSONObject responseBody, boolean status, String message) {
                            CommonLogic.showTestLog(TAG, "showAddVehiclePopupDialog: " + responseBody);
                            Toast.makeText(MyGarageActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (status) {
                                getGarageVehicleList();
                            } else {
                                loadingDialog.dismiss();
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            loadingDialog.dismiss();
                            dialog.dismiss();
                            CommonLogic.showTestLog(TAG, errorMessage);
                        }
                    }
            );
        });

        dialog.setCancelable(true);
        dialog.show();
    }*/

}