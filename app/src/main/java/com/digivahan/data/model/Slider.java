package com.digivahan.data.model;


public class Slider {
    String id, type, type_id, name, image, contact, banner_url;

    public Slider(String type, String type_id, String name, String image, String banner_url) {
        this.type = type;
        this.type_id = type_id;
        this.name = name;
        this.image = image;
        this.banner_url = banner_url;
    }

    public Slider(String id, String type, String type_id, String name, String image, String contact, String banner_url) {
        this.id = id;
        this.type = type;
        this.type_id = type_id;
        this.name = name;
        this.image = image;
        this.contact = contact;
        this.banner_url = banner_url;
    }

    public String getContact() {
        return contact;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return banner_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getBanner_url() {
        return banner_url;
    }

    public void setBanner_url(String banner_url) {
        this.banner_url = banner_url;
    }
}
