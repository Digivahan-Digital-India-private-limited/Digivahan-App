package com.digivahan.ui.Activities.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.digivahan.ui.Activities.BaseActivity;

import com.digivahan.R;
import com.digivahan.data.local.PreferencesManager;
import com.digivahan.databinding.ActivitySplashScreenBinding;
import com.digivahan.ui.Activities.MainActivity;
import com.digivahan.ui.Activities.auth.LoginActivity;
import com.digivahan.ui.Activities.intro.IntroActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    String TAG = "SplashScreenData";
    private ActivitySplashScreenBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        /*Glide.with(this)
                .asGif()
                .load(R.drawable.app_logo_gif)
                .placeholder(R.drawable.app_icon)
                .into(binding.ivLogo);*/


        // Animate logo
        binding.ivLogo.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(1500)
                .withEndAction(() -> {
                    // Navigate after delay
                    navigateNext();
                }).start();
    }

    private void navigateNext() {
        PreferencesManager prefs = new PreferencesManager(this);

        Intent intent;
        if (prefs.getBoolean(PreferencesManager.KEY_FIRST_LAUNCH, true)) {
            intent = new Intent(this, IntroActivity.class);
        } else if (prefs.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

}