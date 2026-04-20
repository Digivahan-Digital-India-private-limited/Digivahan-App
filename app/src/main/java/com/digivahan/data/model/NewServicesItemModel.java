package com.digivahan.data.model;

public class NewServicesItemModel {
    int resource;
    String title, description, buttonText, className;

    public NewServicesItemModel() {}

    public NewServicesItemModel(int resource, String title, String description, String buttonText, String className) {
        this.resource = resource;
        this.title = title;
        this.description = description;
        this.buttonText = buttonText;
        this.className = className;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
