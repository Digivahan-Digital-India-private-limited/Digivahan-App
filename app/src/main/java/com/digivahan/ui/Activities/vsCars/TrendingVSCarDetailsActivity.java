package com.digivahan.ui.Activities.vsCars;

import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.digivahan.R;
import com.digivahan.data.adapters.SectionAdapter;
import com.digivahan.data.api.APIData;
import com.digivahan.data.api.ApiCall;
import com.digivahan.data.model.ComparisonItemModel;
import com.digivahan.data.model.ComparisonSection;
import com.digivahan.data.model.TrendingCarsModel;
import com.digivahan.data.model.TrendingVSCarsModel;
import com.digivahan.databinding.ActivityTrendingVscarDetailsBinding;
import com.digivahan.other.CustomDialog.AshDialog;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrendingVSCarDetailsActivity extends BaseActivity {

    String TAG = "TrendingVSCarDetailsActivityData";
    ActivityTrendingVscarDetailsBinding binding;
    TrendingVSCarsModel trendingVSCarsDetails;

    AshDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrendingVscarDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Comparison");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        try {
            trendingVSCarsDetails = (TrendingVSCarsModel) getIntent().getSerializableExtra("VSDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        loadingDialog = new AshDialog(TrendingVSCarDetailsActivity.this, "Please wait", "");

        // car 1 data
        ImageHelperMethods.loadImage(TAG, TrendingVSCarDetailsActivity.this, Objects.requireNonNull(trendingVSCarsDetails).car1Data.imageUrl.split(",")[0], binding.car1Img, R.drawable.ic_vehicle_default);
        binding.tvCarName1.setText(trendingVSCarsDetails.car1Data.modelName);
        binding.tvPrice1.setText(trendingVSCarsDetails.car1Data.priceDisplay);

        ImageHelperMethods.loadImage(TAG, TrendingVSCarDetailsActivity.this, trendingVSCarsDetails.car2Data.imageUrl.split(",")[0], binding.car2Img, R.drawable.ic_vehicle_default);
        binding.tvCarName2.setText(trendingVSCarsDetails.car2Data.modelName);
        binding.tvPrice2.setText(trendingVSCarsDetails.car2Data.priceDisplay);

        getCarDetails(trendingVSCarsDetails.car1Data.car_id, false);

    }

    private void getCarDetails(String carId, boolean hideProgressDialog) {

        CommonLogic.showTestLog(TAG, "getCarDetails() called");
        CommonLogic.showTestLog(TAG, "Car ID: " + carId);
        CommonLogic.showTestLog(TAG, "hideProgressDialog: " + hideProgressDialog);

        loadingDialog.show();

        ApiCall.callApi(TAG,
                TrendingVSCarDetailsActivity.this,
                APIData.GET_TRENDING_CARS_BY_ID + carId,
                null,
                "get",
                new ApiCall.ApiResponseCallback() {

                    @Override
                    public void onSuccess(JSONObject responseBody, boolean status, String message) {

                        CommonLogic.showTestLog(TAG, "API Success Status: " + status);
                        CommonLogic.showTestLog(TAG, "API Message: " + message);
                        CommonLogic.showTestLog(TAG, "Full Response: " + responseBody);

                        try {

                            if (status) {

                                JSONObject carData = responseBody.getJSONObject("data");
                                CommonLogic.showTestLog(TAG, "Car Data Object: " + carData);

                                JSONObject carDetailsObj = carData.getJSONObject("car_details");
                                CommonLogic.showTestLog(TAG, "Car Details Object: " + carDetailsObj);

                                TrendingCarsModel car = new TrendingCarsModel();

                                // Basic info
                                car.id = carData.optString("_id");
                                car.brandName = carData.optString("brand_name");
                                car.modelName = carData.optString("model_name");
                                car.createdAt = carData.optString("createdAt");

                                CommonLogic.showTestLog(TAG,
                                        "Basic Info -> Brand: " + car.brandName +
                                                ", Model: " + car.modelName);

                                car.type = carDetailsObj.optString("type");
                                car.price = carDetailsObj.optDouble("price");
                                car.priceDisplay = carDetailsObj.optString("price_display");
                                car.mileage = carDetailsObj.optString("mileage");
                                car.topSpeed = carDetailsObj.optString("top_speed");
                                car.imageUrl = carDetailsObj.optString("image_url");

                                // Specifications
                                JSONObject specsObj = carDetailsObj.optJSONObject("specifications");
                                if (specsObj != null) {
                                    CommonLogic.showTestLog(TAG, "Specifications Found");

                                    TrendingCarsModel.Specifications specs =
                                            new TrendingCarsModel.Specifications();

                                    specs.engine_capacity = specsObj.optString("engine_capacity");
                                    specs.transmission = specsObj.optString("transmission");
                                    specs.fuel_tank_capacity = specsObj.optString("fuel_tank_capacity");
                                    specs.seat_height = specsObj.optString("seat_height");
                                    specs.kerb_weight = specsObj.optString("kerb_weight");

                                    car.specifications = specs;
                                }

                                // Detailed specifications
                                JSONObject detailsObj =
                                        carDetailsObj.optJSONObject("detailed_specifications");

                                if (detailsObj != null) {
                                    CommonLogic.showTestLog(TAG,
                                            "Detailed Specifications Found");

                                    TrendingCarsModel.DetailedSpecifications details =
                                            new TrendingCarsModel.DetailedSpecifications();

                                    details.max_power = detailsObj.optString("max_power");
                                    details.max_torque = detailsObj.optString("max_torque");
                                    details.riding_mode = detailsObj.optString("riding_mode");
                                    details.gear_shifting_pattern =
                                            detailsObj.optString("gear_shifting_pattern");

                                    car.detailedSpecifications = details;
                                }

                                // Dimensions
                                JSONObject dimObj = carDetailsObj.optJSONObject("dimensions");
                                if (dimObj != null) {
                                    CommonLogic.showTestLog(TAG, "Dimensions Found");

                                    TrendingCarsModel.Dimensions dim =
                                            new TrendingCarsModel.Dimensions();

                                    dim.bootspace = dimObj.optString("bootspace");
                                    dim.ground_clearance =
                                            dimObj.optString("ground_clearance");
                                    dim.length = dimObj.optString("length");
                                    dim.width = dimObj.optString("width");
                                    dim.height = dimObj.optString("height");

                                    car.dimensions = dim;
                                }

                                // Features
                                JSONObject featuresObj =
                                        carDetailsObj.optJSONObject("features");

                                if (featuresObj != null) {
                                    CommonLogic.showTestLog(TAG, "Features Found");

                                    TrendingCarsModel.Features features =
                                            new TrendingCarsModel.Features();

                                    features.air_conditioner =
                                            featuresObj.optBoolean("air_conditioner");
                                    features.central_locking =
                                            featuresObj.optString("central_locking");
                                    features.power_windows =
                                            featuresObj.optString("power_windows");
                                    features.headrest =
                                            featuresObj.optString("headrest");
                                    features.parking_assist =
                                            featuresObj.optString("parking_assist");
                                    features.cruise_control =
                                            featuresObj.optBoolean("cruise_control");
                                    features.music_system_count =
                                            featuresObj.optInt("music_system_count");
                                    features.apple_carplay =
                                            featuresObj.optString("apple_carplay");
                                    features.android_auto =
                                            featuresObj.optString("android_auto");
                                    features.abs =
                                            featuresObj.optBoolean("abs");
                                    features.sunroof =
                                            featuresObj.optBoolean("sunroof");
                                    features.third_row_ac =
                                            featuresObj.optBoolean("third_row_ac");

                                    JSONArray airbagsArray =
                                            featuresObj.optJSONArray("airbags");

                                    if (airbagsArray != null) {
                                        CommonLogic.showTestLog(TAG,
                                                "Airbags Count: " +
                                                        airbagsArray.length());

                                        List<String> airbagsList =
                                                new ArrayList<>();

                                        for (int j = 0;
                                             j < airbagsArray.length(); j++) {
                                            airbagsList.add(
                                                    airbagsArray.getString(j));
                                        }

                                        features.airbags = airbagsList;
                                    }

                                    car.features = features;
                                }

                                CommonLogic.showTestLog(TAG,
                                        "Car Object Prepared Successfully");

                                if (hideProgressDialog) {

                                    CommonLogic.showTestLog(TAG,
                                            "Assigning as Car 2");

                                    trendingVSCarsDetails.car2Data = car;

                                    binding.rvSections.setLayoutManager(
                                            new LinearLayoutManager(
                                                    TrendingVSCarDetailsActivity.this));

                                    binding.rvSections.setAdapter(
                                            new SectionAdapter(
                                                    buildData(trendingVSCarsDetails),
                                                    (pos, expanded) -> {}));

                                    setView(true);

                                } else {

                                    CommonLogic.showTestLog(TAG,
                                            "Assigning as Car 1");

                                    trendingVSCarsDetails.car1Data = car;

                                    getCarDetails(trendingVSCarsDetails.car2Data.car_id, true);
                                }

                            } else {
                                CommonLogic.showTestLog(TAG,
                                        "API Status False");
                                setView(false);
                            }

                        } catch (JSONException e) {

                            CommonLogic.showTestLog(TAG,
                                    "JSON Exception: " + e.getMessage());

                            setView(false);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                        CommonLogic.showTestLog(TAG,
                                "API Error: " + errorMessage);

                        setView(false);
                    }
                });
    }

    private void setView(boolean isDataSet){
        loadingDialog.dismiss();
        if (isDataSet){
            binding.rvSections.setVisibility(View.VISIBLE);
            binding.emptyText.setVisibility(View.GONE);
        }else {
            binding.rvSections.setVisibility(View.GONE);
            binding.emptyText.setVisibility(View.VISIBLE);
        }
    }

    private List<ComparisonSection> buildData(TrendingVSCarsModel trendingVSCarsDetails) {

        // set cars data

        List<ComparisonSection> list = new ArrayList<>();

        // Overview
        List<ComparisonItemModel> o = new ArrayList<>();
        o.add(new ComparisonItemModel("Maker", trendingVSCarsDetails.car1Data.brandName, trendingVSCarsDetails.car2Data.brandName));
        o.add(new ComparisonItemModel("Model Name", trendingVSCarsDetails.car1Data.modelName, trendingVSCarsDetails.car2Data.modelName));
        o.add(new ComparisonItemModel("Car Type", trendingVSCarsDetails.car1Data.type, trendingVSCarsDetails.car2Data.type));
//        o.add(new ComparisonItemModel("Body Type", "Compact Sedan", "Compact Sedan"));
//        o.add(new ComparisonItemModel("Seating Capacity", "5 Seater", "5 Seater"));
        o.add(new ComparisonItemModel("On Road Price", trendingVSCarsDetails.car1Data.priceDisplay, trendingVSCarsDetails.car2Data.priceDisplay).highlightLeft()); // green pill left
//        o.add(new ComparisonItemModel("NCAP Rating", "4 Star (Global NCAP)", "5 Star (Global NCAP)").highlightRight());
        o.add(new ComparisonItemModel("Mileage", trendingVSCarsDetails.car1Data.mileage, trendingVSCarsDetails.car2Data.mileage).highlightLeft());
        o.add(new ComparisonItemModel("Top Speed", trendingVSCarsDetails.car1Data.topSpeed, trendingVSCarsDetails.car2Data.topSpeed).highlightLeft());
        list.add(new ComparisonSection("Overview", o));

        // Feel The Drive
        List<ComparisonItemModel> f = new ArrayList<>();
        f.add(new ComparisonItemModel("Transmission Type", trendingVSCarsDetails.car1Data.specifications.transmission,
                trendingVSCarsDetails.car2Data.specifications.transmission).highlightRight());
//        f.add(new ComparisonItemModel("Fuel Type", "Petrol", "Petrol"));
        f.add(new ComparisonItemModel("Displacement", trendingVSCarsDetails.car1Data.specifications.engine_capacity, trendingVSCarsDetails.car2Data.specifications.engine_capacity));
        f.add(new ComparisonItemModel("Fuel Tank Capacity", trendingVSCarsDetails.car1Data.specifications.fuel_tank_capacity, trendingVSCarsDetails.car2Data.specifications.fuel_tank_capacity));
        f.add(new ComparisonItemModel("Seat Height", trendingVSCarsDetails.car1Data.specifications.seat_height, trendingVSCarsDetails.car2Data.specifications.seat_height).highlightRight());
        f.add(new ComparisonItemModel("Kerb Weight", trendingVSCarsDetails.car1Data.specifications.kerb_weight, trendingVSCarsDetails.car2Data.specifications.kerb_weight).highlightRight());

        f.add(new ComparisonItemModel("Max Power", trendingVSCarsDetails.car1Data.detailedSpecifications.max_power, trendingVSCarsDetails.car2Data.detailedSpecifications.max_power).highlightRight());
        f.add(new ComparisonItemModel("Max Torque", trendingVSCarsDetails.car1Data.detailedSpecifications.max_torque, trendingVSCarsDetails.car2Data.detailedSpecifications.max_torque).highlightRight());
        f.add(new ComparisonItemModel("Gear Shifting Pattern", trendingVSCarsDetails.car1Data.detailedSpecifications.gear_shifting_pattern, trendingVSCarsDetails.car2Data.detailedSpecifications.gear_shifting_pattern));
//        f.add(new ComparisonItemModel("Fuel Tank Capacity", "37 Liters", "35 Liters").highlightLeft());
//        f.add(new ComparisonItemModel("Emission Standard", "BS6 Phase 2", "BS6 Phase 2"));
        list.add(new ComparisonSection("Feel The Drive", f));

        // Dimension & Size
        List<ComparisonItemModel> d = new ArrayList<>();
        d.add(new ComparisonItemModel("Bootspace", trendingVSCarsDetails.car1Data.dimensions.bootspace, trendingVSCarsDetails.car2Data.dimensions.bootspace).highlightRight());
        d.add(new ComparisonItemModel("Ground Clearance", trendingVSCarsDetails.car1Data.dimensions.ground_clearance, trendingVSCarsDetails.car2Data.dimensions.ground_clearance).highlightLeft());
        d.add(new ComparisonItemModel("Length", trendingVSCarsDetails.car1Data.dimensions.length, trendingVSCarsDetails.car2Data.dimensions.length).highlightRight());
        d.add(new ComparisonItemModel("Width", trendingVSCarsDetails.car1Data.dimensions.width, trendingVSCarsDetails.car2Data.dimensions.width).highlightLeft());
        d.add(new ComparisonItemModel("Height", trendingVSCarsDetails.car1Data.dimensions.height, trendingVSCarsDetails.car2Data.dimensions.height).highlightLeft());
        list.add(new ComparisonSection("Dimension & Size", d));

        // Interior Features (booleans)
        List<ComparisonItemModel> i = new ArrayList<>();
        i.add(ComparisonItemModel.bool("Air Conditioner", trendingVSCarsDetails.car1Data.features.air_conditioner, trendingVSCarsDetails.car2Data.features.air_conditioner));
        d.add(new ComparisonItemModel("Central Locking", trendingVSCarsDetails.car1Data.features.central_locking, trendingVSCarsDetails.car2Data.features.central_locking).highlightLeft());
        d.add(new ComparisonItemModel("Power Windows", trendingVSCarsDetails.car1Data.features.power_windows, trendingVSCarsDetails.car2Data.features.power_windows).highlightLeft());
        d.add(new ComparisonItemModel("Headrest", trendingVSCarsDetails.car1Data.features.headrest, trendingVSCarsDetails.car2Data.features.headrest).highlightLeft());
        d.add(new ComparisonItemModel("Parking Assist", trendingVSCarsDetails.car1Data.features.parking_assist, trendingVSCarsDetails.car2Data.features.parking_assist).highlightLeft());
        i.add(ComparisonItemModel.bool("Cruise Control", trendingVSCarsDetails.car1Data.features.cruise_control, trendingVSCarsDetails.car2Data.features.cruise_control));

        i.add(new ComparisonItemModel("Integrated (in-dash) Music System", String.valueOf(trendingVSCarsDetails.car1Data.features.music_system_count), String.valueOf(trendingVSCarsDetails.car2Data.features.music_system_count)));
        i.add(new ComparisonItemModel("Apple CarPlay", trendingVSCarsDetails.car1Data.features.apple_carplay, trendingVSCarsDetails.car2Data.features.apple_carplay));
        i.add(new ComparisonItemModel("Android Auto", trendingVSCarsDetails.car1Data.features.android_auto, trendingVSCarsDetails.car2Data.features.android_auto).highlightLeft());
        i.add(ComparisonItemModel.bool("Anti-Lock Braking System (ABS)", trendingVSCarsDetails.car1Data.features.abs, trendingVSCarsDetails.car2Data.features.abs));
        i.add(ComparisonItemModel.bool("Sunroof / Moonroof", trendingVSCarsDetails.car1Data.features.sunroof, trendingVSCarsDetails.car2Data.features.sunroof));
        i.add(ComparisonItemModel.bool("Anti-Lock Third Row AC", trendingVSCarsDetails.car1Data.features.third_row_ac, trendingVSCarsDetails.car2Data.features.third_row_ac));
        i.add(new ComparisonItemModel("Airbags", trendingVSCarsDetails.car1Data.features.getAirbagsAsString(), trendingVSCarsDetails.car2Data.features.getAirbagsAsString()));
        list.add(new ComparisonSection("Interior Features", i));

        return list;
    }
}