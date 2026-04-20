package com.digivahan.ui.Fragments.myGarageInfoPages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.digivahan.data.model.GarageItemModel;
import com.digivahan.databinding.FragmentDetailsBinding;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.TimeUtils;

public class DetailsFragment extends Fragment {
    String TAG = "DetailsFragmentData";

    FragmentDetailsBinding binding;
    GarageItemModel vehicleInfo;

    public DetailsFragment(GarageItemModel vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsBinding.inflate(getLayoutInflater(), container, false);

        try {

            // Ownership details
            binding.ownershipDetails.tvOwnerName.setText(vehicleInfo.getOwner_name());
            binding.ownershipDetails.ownership.setText(CommonMethods.getFormattedOwner(vehicleInfo.getOwnership_details()));
            binding.ownershipDetails.registrationDate.setText(TimeUtils.convertDateFormat(vehicleInfo.getRegistration_date(), "dd MMM yyyy"));
            binding.ownershipDetails.registeredRTO.setText(vehicleInfo.getRegistered_rto());
            binding.ownershipDetails.contentLayout.setVisibility(View.GONE);
            binding.ownershipDetails.ivExpand.setRotation(180f);

            if (vehicleInfo.getFinancer_name() != null && !vehicleInfo.getFinancer_name().isEmpty() && !vehicleInfo.getFinancer_name().equalsIgnoreCase("NA")
                    && !vehicleInfo.getFinancer_name().equalsIgnoreCase("N/A")) {
                binding.ownershipDetails.financerNameLayout.setVisibility(View.VISIBLE);
                binding.ownershipDetails.financerName.setText(vehicleInfo.getFinancer_name());
            } else {
                binding.ownershipDetails.financerNameLayout.setVisibility(View.GONE);
            }

            binding.ownershipDetails.headingLayout.setOnClickListener(view -> {
                if (binding.ownershipDetails.contentLayout.getVisibility() == View.VISIBLE) {
                    collapseContent(binding.ownershipDetails.contentLayout,
                            binding.ownershipDetails.ivExpand);
                } else {
                    expandContent(binding.ownershipDetails.contentLayout,
                            binding.ownershipDetails.ivExpand);
                }
            });


            // Vehicle details
            binding.vehicleDetails.tvVehicleModel.setText(vehicleInfo.getMakers_model());
            binding.vehicleDetails.vehicleClass.setText(vehicleInfo.getVehicle_class());
            binding.vehicleDetails.fuelType.setText(vehicleInfo.getFuel_type());
            binding.vehicleDetails.fuelNorms.setText(vehicleInfo.getFuel_norms());
            binding.vehicleDetails.engineNo.setText(vehicleInfo.getEngine());
            binding.vehicleDetails.chassisNo.setText(vehicleInfo.getChassis_number());
            binding.vehicleDetails.contentLayout.setVisibility(View.GONE);
            binding.vehicleDetails.ivExpand.setRotation(180f);
            binding.vehicleDetails.headingLayout.setOnClickListener(view -> {
                if (binding.vehicleDetails.contentLayout.getVisibility() == View.VISIBLE) {
                    collapseContent(binding.vehicleDetails.contentLayout,
                            binding.vehicleDetails.ivExpand);
                } else {
                    expandContent(binding.vehicleDetails.contentLayout,
                            binding.vehicleDetails.ivExpand);
                }
            });


            // Important Dates
            binding.importantDates.insuranceExpiryData.setText(TimeUtils.convertDateFormat(vehicleInfo.getInsurance_expiry(), "dd MMM yyyy"));
            int insuranceDaysLeft = (int) TimeUtils.getDaysDifference(vehicleInfo.getInsurance_expiry(),
                    CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"));

            if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), vehicleInfo.getInsurance_expiry())) {
                binding.importantDates.insuranceExpiryDaysLeft.setText("Expired");
            } else {
                binding.importantDates.insuranceExpiryDaysLeft.setText(insuranceDaysLeft + " Days Left");
            }

            binding.importantDates.fitnessUpto.setText(TimeUtils.convertDateFormat(vehicleInfo.getFitness_upto(), "dd MMM yyyy"));

            binding.importantDates.vehicleAge.setText(CommonLogic.getVehicleAge(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), TimeUtils.convertDateFormat(vehicleInfo.getRegistration_date(), "dd MMM yyyy")));
            binding.importantDates.registrationDate.setText(TimeUtils.convertDateFormat(vehicleInfo.getRegistration_date(), "dd MMM yyyy"));
            binding.importantDates.pollutionUpto.setText(TimeUtils.convertDateFormat(vehicleInfo.getPollution_expiry(), "dd MMM yyyy"));

            binding.importantDates.PUCExpiring.setText(TimeUtils.convertDateFormat(vehicleInfo.getPollution_expiry(), "dd MMM yyyy"));
            int PUCDaysLeft = (int) TimeUtils.getDaysDifference(vehicleInfo.getPollution_expiry(),
                    CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"));

            if (TimeUtils.isDateExpired(CommonMethods.getCurrentDate(getContext(), "dd MMM yyyy"), vehicleInfo.getPollution_expiry())) {
                binding.importantDates.PUCExpiring.setText("Expired");
            } else {
                binding.importantDates.PUCExpiring.setText(PUCDaysLeft + " Days Left");
            }

            binding.importantDates.contentLayout.setVisibility(View.GONE);
            binding.importantDates.ivExpand.setRotation(180f);
            binding.importantDates.headingLayout.setOnClickListener(view -> {
                if (binding.importantDates.contentLayout.getVisibility() == View.VISIBLE) {
                    collapseContent(binding.importantDates.contentLayout,
                            binding.importantDates.ivExpand);
                } else {
                    expandContent(binding.importantDates.contentLayout,
                            binding.importantDates.ivExpand);
                }
            });

            // Other Details
            binding.otherDetails.vehicleColor.setText(vehicleInfo.getColor());
            binding.otherDetails.unloadedWeight.setText(vehicleInfo.getUnloaded_weight());
            binding.otherDetails.RCStatus.setText(vehicleInfo.getRc_status());
            binding.otherDetails.contentLayout.setVisibility(View.GONE);
            binding.otherDetails.ivExpand.setRotation(180f);
            binding.otherDetails.headingLayout.setOnClickListener(view -> {
                if (binding.otherDetails.contentLayout.getVisibility() == View.VISIBLE) {
                    collapseContent(binding.otherDetails.contentLayout,
                            binding.otherDetails.ivExpand);
                } else {
                    expandContent(binding.otherDetails.contentLayout,
                            binding.otherDetails.ivExpand);
                }
            });

            // Insurance details
            binding.insuranceDetails.insurerName.setText(vehicleInfo.getInsurer_name());
            binding.insuranceDetails.insurerPolicyNo.setText(vehicleInfo.getInsurance_policy_number());
            binding.insuranceDetails.InsuranceExpiryDate.setText(TimeUtils.convertDateFormat(vehicleInfo.getInsurance_expiry(), "dd MMM yyyy"));

            binding.insuranceDetails.contentLayout.setVisibility(View.GONE);
            binding.insuranceDetails.ivExpand.setRotation(180f);
            binding.insuranceDetails.headingLayout.setOnClickListener(view -> {
                if (binding.insuranceDetails.contentLayout.getVisibility() == View.VISIBLE) {
                    collapseContent(binding.insuranceDetails.contentLayout,
                            binding.insuranceDetails.ivExpand);
                } else {
                    expandContent(binding.insuranceDetails.contentLayout,
                            binding.insuranceDetails.ivExpand);
                }
            });

        } catch (Exception e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
//            throw new RuntimeException(e);
        }

        return binding.getRoot();
    }

    private void expandContent(View contentLayout, ImageView arrow) {
        contentLayout.setVisibility(View.VISIBLE);

        // Start ABOVE and move DOWN
        contentLayout.setAlpha(0f);
        contentLayout.setTranslationY(-contentLayout.getHeight());

        contentLayout.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Arrow back to original position
        arrow.animate()
                .rotation(0f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }


    private void collapseContent(View contentLayout, ImageView arrow) {
        contentLayout.animate()
                .translationY(-contentLayout.getHeight())
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    contentLayout.setVisibility(View.GONE);
                    contentLayout.setTranslationY(0f);
                })
                .start();

        // Rotate arrow 180°
        arrow.animate()
                .rotation(180f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }


}