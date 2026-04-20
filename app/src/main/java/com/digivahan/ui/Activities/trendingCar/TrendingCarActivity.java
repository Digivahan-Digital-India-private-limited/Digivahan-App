package com.digivahan.ui.Activities.trendingCar;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.adapters.ImageSliderAdapter;
import com.digivahan.data.model.TrendingCarsModel;
import com.digivahan.databinding.ActivityTrendingCarBinding;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

public class TrendingCarActivity extends BaseActivity {

    String TAG = "TrendingCarActivityData";
    ActivityTrendingCarBinding binding;

    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;

    TrendingCarsModel carsModelDetails;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrendingCarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Trending Car");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });

        try {
            carsModelDetails = (TrendingCarsModel) getIntent().getSerializableExtra("carDetails");
        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }

        setCarData(carsModelDetails);


    }

    private void setCarData(TrendingCarsModel carsModelDetails) {
        if (carsModelDetails != null){
            // Sample images
            List<String> imageList = new ArrayList<>();
// Split by comma and trim any extra spaces
            String[] urls = carsModelDetails.imageUrl.split(",");
            for (String url : urls) {
                imageList.add(url.trim());
            }

// ✅ Test Log
            for (String img : imageList) {
                CommonLogic.showTestLog("ImageList", "URL: " + img);
            }

            ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageList);
            binding.imageViewPager.setAdapter(adapter);

            // Link the dots indicator with ViewPager2
            binding.dotsIndicator.attachTo(binding.imageViewPager);

            // Auto-slide every 3 seconds
            sliderRunnable = new Runnable() {
                @Override
                public void run() {
                    int nextPosition = (binding.imageViewPager.getCurrentItem() + 1) % imageList.size();
                    binding.imageViewPager.setCurrentItem(nextPosition, true);
                    sliderHandler.postDelayed(this, 3000);
                }
            };
            sliderHandler.postDelayed(sliderRunnable, 3000);

            // Smooth page transition
            binding.imageViewPager.setPageTransformer((page, position) -> {
                float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
                page.setScaleY(scale);
                page.setAlpha(0.8f + (1 - Math.abs(position)) * 0.2f);
            });

            /*binding.carPrice.setText(carsModelDetails.priceDisplay);
            binding.brandName.setText("Brand Name : " + carsModelDetails.brandName);
            binding.carType.setText("Type : " + carsModelDetails.type);
            binding.carMileage.setText("Mileage : " + carsModelDetails.mileage);
            binding.carTopSpeed.setText("Top Speed : " + carsModelDetails.topSpeed);
            binding.engineCapacity.setText(carsModelDetails.specifications.engine_capacity);
            binding.transmission.setText(carsModelDetails.specifications.transmission);
            binding.fuelTankCapacity.setText(carsModelDetails.specifications.fuel_tank_capacity);
            binding.maxPower.setText(carsModelDetails.detailedSpecifications.max_power);
            binding.maxTorque.setText(carsModelDetails.detailedSpecifications.max_torque);
            binding.ridingMode.setText(carsModelDetails.detailedSpecifications.riding_mode);
            binding.seatHeight.setText(carsModelDetails.specifications.seat_height);
            binding.kerbWeight.setText(carsModelDetails.specifications.kerb_weight);
            //            binding.gearShiftingPattern.setText(carsModelDetails.specifications.);
            */
// Price
            binding.carPrice.setText(
                    CommonMethods.safeValue(carsModelDetails.priceDisplay, TAG)
            );

// Brand Name
            binding.brandName.setText(
                    "Brand Name : " + CommonMethods.safeValue(carsModelDetails.brandName, TAG)
            );

            // Car Model
            binding.modelName.setText(
                    "Model Name : " + CommonMethods.safeValue(carsModelDetails.modelName, TAG)
            );

// Type
            binding.carType.setText(
                    "Type : " + CommonMethods.safeValue(carsModelDetails.type, TAG)
            );

// Mileage
            binding.carMileage.setText(
                    "Mileage : " + CommonMethods.safeValue(carsModelDetails.mileage, TAG)
            );

// Top Speed
            binding.carTopSpeed.setText(
                    "Top Speed : " + CommonMethods.safeValue(carsModelDetails.topSpeed, TAG)
            );

// ---- Specifications (Null Safe) ----
            if (carsModelDetails.specifications != null) {

                binding.engineCapacity.setText(
                        CommonMethods.safeValue(carsModelDetails.specifications.engine_capacity, TAG)
                );

                binding.transmission.setText(
                        CommonMethods.safeValue(carsModelDetails.specifications.transmission, TAG)
                );

                binding.fuelTankCapacity.setText(
                        CommonMethods.safeValue(carsModelDetails.specifications.fuel_tank_capacity, TAG)
                );

                binding.seatHeight.setText(
                        CommonMethods.safeValue(carsModelDetails.specifications.seat_height, TAG)
                );

                binding.kerbWeight.setText(
                        CommonMethods.safeValue(carsModelDetails.specifications.kerb_weight, TAG)
                );

            } else {
                Log.e(TAG, "Specifications object is NULL");

                binding.engineCapacity.setText("N/A");
                binding.transmission.setText("N/A");
                binding.fuelTankCapacity.setText("N/A");
                binding.seatHeight.setText("N/A");
                binding.kerbWeight.setText("N/A");
            }

// ---- Detailed Specifications (Null Safe) ----
            if (carsModelDetails.detailedSpecifications != null) {

                binding.maxPower.setText(
                        CommonMethods.safeValue(carsModelDetails.detailedSpecifications.max_power, TAG)
                );

                binding.maxTorque.setText(
                        CommonMethods.safeValue(carsModelDetails.detailedSpecifications.max_torque, TAG)
                );

                binding.ridingMode.setText(
                        CommonMethods.safeValue(carsModelDetails.detailedSpecifications.riding_mode, TAG)
                );

            } else {
                Log.e(TAG, "DetailedSpecifications object is NULL");

                binding.maxPower.setText("N/A");
                binding.maxTorque.setText("N/A");
                binding.ridingMode.setText("N/A");
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

}