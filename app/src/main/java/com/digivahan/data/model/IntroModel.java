package com.digivahan.data.model;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

public class IntroModel {
    public int imageRes;
    public String title;
    public String description;

    public IntroModel(int imageRes, String title, String description) {
        this.imageRes = imageRes;
        this.title = title;
        this.description = description;
    }


    @BindingAdapter("imageRes")
    public static void setImageRes(ImageView imageView, int resId) {
        imageView.setImageResource(resId);
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}

