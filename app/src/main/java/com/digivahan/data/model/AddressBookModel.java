package com.digivahan.data.model;


import java.io.Serializable;

public class AddressBookModel implements Serializable {
   String _id, name, contact_no, house_no_building, street_name, landmark, road_or_area, city, state, pincode;

   boolean default_status;

    public AddressBookModel() {}

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getHouse_no_building() {
        return house_no_building;
    }

    public void setHouse_no_building(String house_no_building) {
        this.house_no_building = house_no_building;
    }

    public String getRoad_or_area() {
        return road_or_area;
    }

    public void setRoad_or_area(String road_or_area) {
        this.road_or_area = road_or_area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public boolean isDefault_status() {
        return default_status;
    }

    public String getStreet_name() {
        return street_name;
    }

    public void setStreet_name(String street_name) {
        this.street_name = street_name;
    }

    public void setDefault_status(boolean default_status) {
        this.default_status = default_status;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}
