package com.digivahan.other;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.digivahan.R;

public class HideContentLayout{

    public View secureView;

    Activity activity;

    private boolean showHideLayout = true;

    public HideContentLayout(Activity activity) {
        this.activity = activity;

        if (secureView == null) {
            secureView = LayoutInflater.from(activity)
                    .inflate(R.layout.layout_secure_preview, null);
            activity.addContentView(secureView,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
        }
    }

    public void showHideContentLayout() {
        if (secureView != null && showHideLayout) {
            secureView.setVisibility(View.VISIBLE);
        }
    }

    public void hideContentLayout() {
        if (secureView != null) {
            secureView.setVisibility(View.GONE);
        }
    }

    public void setHideContentLayout(boolean showHideLayout) {
        this.showHideLayout = showHideLayout;
    }
}
