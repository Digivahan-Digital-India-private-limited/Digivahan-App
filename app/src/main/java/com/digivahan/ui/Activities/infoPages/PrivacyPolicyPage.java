package com.digivahan.ui.Activities.infoPages;

import android.os.Bundle;
import android.view.View;

import com.digivahan.ui.Activities.BaseActivity;
import androidx.core.content.ContextCompat;

import com.digivahan.R;
import com.digivahan.databinding.ActivityPrivacyPolicyPageBinding;
import com.digivahan.databinding.ActivityTermsConditionPageBinding;
import com.digivahan.utils.CommonMethods;

public class PrivacyPolicyPage extends BaseActivity {

    ActivityPrivacyPolicyPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonMethods.setStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.white)
        );

        binding.toolbarLayout.tvTitle.setText("Privacy Policy");
        binding.toolbarLayout.ivBell.setVisibility(View.INVISIBLE);

        binding.toolbarLayout.ivProfileLayout.setVisibility(View.GONE);
        binding.toolbarLayout.backBtn.setVisibility(View.VISIBLE);
        binding.toolbarLayout.backBtn.setOnClickListener(view -> { onBackPressed(); });

    }
}