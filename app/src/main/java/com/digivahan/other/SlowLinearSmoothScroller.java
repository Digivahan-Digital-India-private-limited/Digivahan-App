package com.digivahan.other;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class SlowLinearSmoothScroller extends LinearSmoothScroller {

    public SlowLinearSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        // Higher value = slower scroll
        return 200f / displayMetrics.densityDpi;
    }
}