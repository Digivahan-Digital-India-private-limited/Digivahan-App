package com.digivahan.data.model;

import java.io.Serializable;

public class User implements Serializable {
    String first_name, last_name, phone_number, email,
            is_phone_number_primary, phone_number_verified, profile_pic, profile_id, public_pic, public_id, is_email_verified, is_email_primary,
            password, occupation, profile_completion_percent,
            nick_name, address, age, gender, vehicleId, userId;

    public User() {}

    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public String getPublic_id() {
        return public_id;
    }

    public void setPublic_id(String public_id) {
        this.public_id = public_id;
    }

    public String getIs_phone_number_primary() {
        return is_phone_number_primary;
    }

    public void setIs_phone_number_primary(String is_phone_number_primary) {
        this.is_phone_number_primary = is_phone_number_primary;
    }

    public String getPublic_pic() {
        return public_pic;
    }

    public void setPublic_pic(String public_pic) {
        this.public_pic = public_pic;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number_verified() {
        return phone_number_verified;
    }

    public void setPhone_number_verified(String phone_number_verified) {
        this.phone_number_verified = phone_number_verified;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getIs_email_verified() {
        return is_email_verified;
    }

    public void setIs_email_verified(String is_email_verified) {
        this.is_email_verified = is_email_verified;
    }

    public String getIs_email_primary() {
        return is_email_primary;
    }

    public void setIs_email_primary(String is_email_primary) {
        this.is_email_primary = is_email_primary;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getProfile_completion_percent() {
        return profile_completion_percent;
    }

    public void setProfile_completion_percent(String profile_completion_percent) {
        this.profile_completion_percent = profile_completion_percent;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

