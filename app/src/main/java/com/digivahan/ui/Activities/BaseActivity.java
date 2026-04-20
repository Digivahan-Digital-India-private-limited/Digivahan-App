package com.digivahan.ui.Activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;


import com.digivahan.R;

public class BaseActivity extends AppCompatActivity {

    private View secureView;

    // ⭐ THIS IS THE CONTROL FLAG
    protected boolean shouldShowSecure = true;

    @Override
    protected void onPause() {
        super.onPause();

        // ONLY show when flag is true
        if (shouldShowSecure) {
            showSecureView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSecureView();

        // Reset for next time
        shouldShowSecure = true;
    }

    public void disableHideContentSecureForNextNavigation() {
        shouldShowSecure = false;
    }

    private void showSecureView() {
        if (secureView == null) {
            secureView = LayoutInflater.from(this)
                    .inflate(R.layout.layout_secure_preview, null);

            addContentView(secureView,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
        }

        secureView.setVisibility(View.VISIBLE);
    }

    private void hideSecureView() {
        if (secureView != null) {
            secureView.setVisibility(View.GONE);
        }
    }
}


