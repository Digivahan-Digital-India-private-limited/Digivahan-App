package com.digivahan.data.model;

public class NotificationRequestCardItemModel {
    private int iconRes;
    private String title, requestId;

    public NotificationRequestCardItemModel() {}

    public NotificationRequestCardItemModel(int iconRes, String title, String requestId) {
        this.iconRes = iconRes;
        this.title = title;
        this.requestId = requestId;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
