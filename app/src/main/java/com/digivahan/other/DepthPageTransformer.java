package com.digivahan.other;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class DepthPageTransformer implements ViewPager.PageTransformer {
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0f);
        } else if (position <= 0) { // [-1,0]
            view.setAlpha(1f);
            view.setTranslationX(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
        } else if (position <= 1) { // (0,1]
            view.setAlpha(1 - position);
            view.setTranslationX(pageWidth * -position);
            float scaleFactor = 0.75f + (1 - 0.75f) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setAlpha(0f);
        }
    }
}

