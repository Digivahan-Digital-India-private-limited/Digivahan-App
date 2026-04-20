package com.digivahan.data.model;

public class OtherServicesModel {
    String serviceTitle;
    int drawableFile;

    public OtherServicesModel() {}

    public OtherServicesModel(String serviceTitle, int drawableFile) {
        this.serviceTitle = serviceTitle;
        this.drawableFile = drawableFile;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public int getDrawableFile() {
        return drawableFile;
    }

    public void setDrawableFile(int drawableFile) {
        this.drawableFile = drawableFile;
    }
}
