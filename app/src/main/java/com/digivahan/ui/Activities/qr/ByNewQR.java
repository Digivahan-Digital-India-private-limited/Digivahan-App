package com.digivahan.ui.Activities.qr;

import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.databinding.ActivityByNewQrBinding;
import com.digivahan.ui.Activities.orderDetails.OrderQRPage;
import com.digivahan.utils.CommonMethods;

public class ByNewQR extends BaseActivity {

    ActivityByNewQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityByNewQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);
        binding.toolbarLayout.tvTitle.setText("My Order");

        binding.toolbarLayout.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                disableHideContentSecureForNextNavigation();
                finish();
            }
        });
    }
}