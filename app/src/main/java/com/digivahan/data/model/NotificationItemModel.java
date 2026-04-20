package com.digivahan.data.model;


import java.io.Serializable;
import java.util.List;


public class NotificationItemModel implements Serializable {

    private String sender_id, sender_pic, sender_name, notification_type, notification_title,
            link, vehicle_id, order_id, message, issue_type, chat_room_id, latitude,
            longitude, _id, time, createdAt, updatedAt;
    private boolean seen_status, inapp_notification;

    private List<String> incident_proof;

    public NotificationItemModel() {}



    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getSender_pic() {
        return sender_pic;
    }

    public void setSender_pic(String sender_pic) {
        this.sender_pic = sender_pic;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }

    public String getNotification_title() {
        return notification_title;
    }

    public void setNotification_title(String notification_title) {
        this.notification_title = notification_title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIssue_type() {
        return issue_type;
    }

    public void setIssue_type(String issue_type) {
        this.issue_type = issue_type;
    }

    public String getChat_room_id() {
        return chat_room_id;
    }

    public void setChat_room_id(String chat_room_id) {
        this.chat_room_id = chat_room_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public List<String> getIncident_proof() {
        return incident_proof;
    }

    public void setIncident_proof(List<String> incident_proof) {
        this.incident_proof = incident_proof;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSeen_status() {
        return seen_status;
    }

    public void setSeen_status(boolean seen_status) {
        this.seen_status = seen_status;
    }

    public boolean isInapp_notification() {
        return inapp_notification;
    }

    public void setInapp_notification(boolean inapp_notification) {
        this.inapp_notification = inapp_notification;
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

