package com.digivahan.ui.Activities.intro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.digivahan.ui.Activities.BaseActivity;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.data.model.IntroModel;
import com.digivahan.databinding.ActivityIntroBinding;
import com.digivahan.ui.Activities.auth.LoginActivity;
import com.digivahan.utils.CommonLogic;

import java.util.Arrays;
import java.util.List;

public class IntroActivity extends AppCompatActivity {
    String TAG = "IntroActivityData";
    private ActivityIntroBinding binding;
    private List<IntroModel> introList;
    private int currentIndex = 0;

    PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonLogic.hideStatusBar(IntroActivity.this);

        prefs = new PreferencesManager(this);

        // Prepare intro pages
        introList = Arrays.asList(
                new IntroModel(R.drawable.intro_img_1, "Scan & Connect",
                        "Scan the QR code on any vehicle to instantly connect with its owner. Quick, safe, and hassle-free communication."),
                new IntroModel(R.drawable.intro_img_2, "Nearby Essentials",
                        "Find nearby services like mechanics, petrol pumps, and towing – all based on your current location, just one tap away."),
                new IntroModel(R.drawable.intro_img_3, "Vehicle Info & Challan",
                        "Check challans, insurance, and PUC details of any vehicle with ease. All info comes from trusted government sources."),
                new IntroModel(R.drawable.app_icon, "Welcome to Digivahan",
                        "Simplifying the way you connect with vehicles.\u2028From instant owner contact to complete vehicle info – everything is just a tap away.")
        );

        // First page load
        setIntroPage(currentIndex);

        binding.btnNext.setOnClickListener(v -> {
            if (currentIndex < introList.size() - 1) {
                currentIndex++;
                setIntroPage(currentIndex);
                if (currentIndex == introList.size() - 1) {
                    binding.btnSkip.setVisibility(View.GONE);
                    binding.btnNext.setText("Get Started");
                }
            } else {
                // Finished intro → go to Login/Main
                prefs.setBoolean(PreferencesManager.KEY_FIRST_LAUNCH, false);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        binding.btnSkip.setOnClickListener(v -> {
            prefs.setBoolean(PreferencesManager.KEY_FIRST_LAUNCH, false);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

    }

    private void setIntroPage(int index) {
        IntroModel model = introList.get(index);
        binding.setIntroData(model);

        // manually set image
        binding.introImage.setImageResource(model.getImageRes());

        Log.d(TAG, "ImageRes ID: " + model.getImageRes());


        updateIndicators(index);
    }

    private void updateIndicators(int index) {
        binding.indicatorLayout.removeAllViews();

        int margin = (int) getResources().getDimension(R.dimen.indicator_margin);

        for (int i = 0; i < introList.size(); i++) {
            View view = new View(this);

            // Width: 16dp for active, 8dp for inactive
            int width = (int) getResources().getDimension(i == index
                    ? R.dimen.indicator_active_width
                    : R.dimen.indicator_inactive_width);

            int height = (int) getResources().getDimension(R.dimen.indicator_height);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(margin, 0, margin, 0);
            view.setLayoutParams(params);

            // Set drawable
            view.setBackgroundResource(i == index
                    ? R.drawable.indicator_active
                    : R.drawable.indicator_inactive);

            binding.indicatorLayout.addView(view);
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        currentIndex--;
        if (currentIndex == -1) {
            super.onBackPressed();
        } else {
            binding.btnSkip.setVisibility(View.VISIBLE);
            binding.btnNext.setText("Next");
            setIntroPage(currentIndex);
        }


    }
}