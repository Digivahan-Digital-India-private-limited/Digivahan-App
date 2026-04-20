package com.digivahan.ui.Activities.primaryDetails;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivityPrimaryOptionsPageBinding;
import com.digivahan.ui.Activities.deliveryAddress.SetDefaultAddressPage;
import com.digivahan.ui.Activities.orderDetails.OrderQRPage;
import com.digivahan.utils.CommonLogic;
import com.digivahan.utils.CommonMethods;
import com.ashu.ashuutils.fileUtils.image.ImageHelperMethods;

public class PrimaryOptionsPage extends BaseActivity {
    String TAG = "PrimaryOptionsPageData";

    ActivityPrimaryOptionsPageBinding binding;
    PreferencesManager manager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrimaryOptionsPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("Profile update");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> back());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });

        manager = new PreferencesManager(PrimaryOptionsPage.this);

        ImageHelperMethods.loadImage(TAG, PrimaryOptionsPage.this, manager.getUser().getProfile_pic(), binding.primaryImgProfile, R.drawable.temp_profile_icon);
        try {

            CommonLogic.showTestLog(TAG, "User Data: "+manager.getUser().getFirst_name() + " " + manager.getUser().getLast_name());

            binding.userPrimaryName.setText(manager.getUser().getFirst_name() + " " + manager.getUser().getLast_name());
            binding.primaryProgressCircle.setProgress(Integer.parseInt(manager.getUser().getProfile_completion_percent()));
            binding.primaryProgressText.setText(manager.getUser().getProfile_completion_percent() + "% Complete");
        } catch (NumberFormatException e) {
            CommonLogic.showTestLog(TAG, e.getMessage());
        }


        binding.primaryDetailBtn.ivIcon.setImageResource(R.drawable.basic_detail_icon);
        binding.primaryDetailBtn.tvLabel.setText("Primary Details");

        binding.defaultAddressBtn.ivIcon.setImageResource(R.drawable.address_icon);
        binding.defaultAddressBtn.tvLabel.setText("Default Address");

        binding.primaryDetailBtn.mainLayout.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(PrimaryOptionsPage.this, SetPrimaryContactPage.class);
            startActivity(basicProfile);
        });

        binding.defaultAddressBtn.mainLayout.setOnClickListener(v -> {
            disableHideContentSecureForNextNavigation();
            Intent basicProfile = new Intent(PrimaryOptionsPage.this, SetDefaultAddressPage.class);
            startActivity(basicProfile);
        });

    }

    private void back() {
        disableHideContentSecureForNextNavigation();
        finish();
    }
}