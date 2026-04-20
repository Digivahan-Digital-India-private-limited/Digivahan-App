package com.digivahan.data.model;

public class VehicleServiceItemModel {
    String title, serviceType;
    int icon;

    public VehicleServiceItemModel() {}

    public VehicleServiceItemModel(String title, String serviceType, int icon) {
        this.title = title;
        this.serviceType = serviceType;
        this.icon = icon;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
