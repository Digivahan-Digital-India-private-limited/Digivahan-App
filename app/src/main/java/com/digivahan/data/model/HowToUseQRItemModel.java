package com.digivahan.data.model;

public class HowToUseQRItemModel {
    int imageResource;
    String message;

    public HowToUseQRItemModel(int imageResource, String message) {
        this.imageResource = imageResource;
        this.message = message;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
